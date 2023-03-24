package org.odk.collect.android.formmanagement

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.projects.ProjectDependencyProvider
import org.odk.collect.android.projects.ProjectDependencyProviderFactory
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.androidshared.data.getState
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSourceException
import org.odk.collect.settings.keys.ProjectKeys
import java.io.File
import java.util.function.Supplier
import java.util.stream.Collectors

class FormsDataService(
    private val context: Context,
    private val notifier: Notifier,
    private val projectDependencyProviderFactory: ProjectDependencyProviderFactory,
    private val clock: Supplier<Long>
) {

    private val appState = context.getState()

    fun getForms(projectId: String): LiveData<List<Form>?> {
        return getFormsLiveData(projectId)
    }

    fun isSyncing(projectId: String): LiveData<Boolean> {
        return getSyncingLiveData(projectId)
    }

    fun getSyncError(projectId: String): LiveData<FormSourceException?> {
        return getSyncErrorLiveData(projectId)
    }

    fun getDiskError(projectId: String): LiveData<String?> {
        return getDiskErrorLiveData(projectId)
    }

    fun clear(projectId: String) {
        getSyncingLiveData(projectId).value = false
        getSyncErrorLiveData(projectId).value = null
    }

    /**
     * Downloads updates for the project's already downloaded forms. If Automatic download is
     * disabled the user will just be notified that there are updates available.
     */
    fun downloadUpdates(projectId: String) {
        val sandbox = projectDependencyProviderFactory.create(projectId)

        val diskFormsSynchronizer = diskFormsSynchronizer(sandbox)
        val serverFormsDetailsFetcher = serverFormsDetailsFetcher(sandbox, diskFormsSynchronizer)
        val formDownloader = formDownloader(sandbox, clock)

        try {
            val serverForms: List<ServerFormDetails> = serverFormsDetailsFetcher.fetchFormDetails()
            val updatedForms =
                serverForms.stream().filter { obj: ServerFormDetails -> obj.isUpdated }
                    .collect(Collectors.toList())
            if (updatedForms.isNotEmpty()) {
                if (sandbox.generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE)) {
                    val formUpdateDownloader = FormUpdateDownloader()
                    val results = formUpdateDownloader.downloadUpdates(
                        updatedForms,
                        sandbox.formsLock,
                        formDownloader
                    )

                    notifier.onUpdatesDownloaded(results, projectId)
                } else {
                    notifier.onUpdatesAvailable(updatedForms, projectId)
                }
            }

            update(projectId)
            context.contentResolver.notifyChange(FormsContract.getUri(projectId), null)
        } catch (_: FormSourceException) {
            // Ignored
        }
    }

    /**
     * Downloads new forms, updates existing forms and deletes forms that are no longer part of
     * the project's form list.
     */
    @JvmOverloads
    fun matchFormsWithServer(projectId: String, notify: Boolean = true): Boolean {
        val sandbox = projectDependencyProviderFactory.create(projectId)

        val diskFormsSynchronizer = diskFormsSynchronizer(sandbox)
        val serverFormsDetailsFetcher = serverFormsDetailsFetcher(sandbox, diskFormsSynchronizer)
        val formDownloader = formDownloader(sandbox, clock)

        val serverFormsSynchronizer = ServerFormsSynchronizer(
            serverFormsDetailsFetcher,
            sandbox.formsRepository,
            sandbox.instancesRepository,
            formDownloader
        )

        return sandbox.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                startSync(projectId)

                val exception = try {
                    serverFormsSynchronizer.synchronize()
                    finishSync(projectId, null)
                    if (notify) {
                        notifier.onSync(null, projectId)
                    }
                    null
                } catch (e: FormSourceException) {
                    finishSync(projectId, e)
                    if (notify) {
                        notifier.onSync(e, projectId)
                    }
                    e
                }

                update(projectId)
                exception == null
            } else {
                false
            }
        }
    }

    fun deleteForm(projectId: String, formId: Long) {
        val sandbox = projectDependencyProviderFactory.create(projectId)
        FormDeleter(sandbox.formsRepository, sandbox.instancesRepository).delete(formId)
        update(projectId)
    }

    fun all(projectId: String): List<Form> {
        val sandbox = projectDependencyProviderFactory.create(projectId)
        return sandbox.formsRepository.all
    }

    fun update(projectId: String) {
        syncWithStorage(projectId)
        getFormsLiveData(projectId).postValue(all(projectId))
    }

    private fun syncWithStorage(projectId: String) {
        val sandbox = projectDependencyProviderFactory.create(projectId)
        sandbox.changeLockProvider.getFormLock(projectId).withLock { acquiredLock ->
            if (acquiredLock) {
                val error = diskFormsSynchronizer(sandbox).synchronizeAndReturnError()
                getDiskErrorLiveData(projectId).postValue(error)
            }
        }
    }

    private fun startSync(projectId: String) {
        getSyncingLiveData(projectId).postValue(true)
    }

    private fun finishSync(projectId: String, exception: FormSourceException?) {
        getSyncErrorLiveData(projectId).postValue(exception)
        getSyncingLiveData(projectId).postValue(false)
        context.contentResolver.notifyChange(FormsContract.getUri(projectId), null)
    }

    private fun getFormsLiveData(projectId: String): MutableLiveData<List<Form>?> {
        return appState.get("forms:$projectId", MutableLiveData())
    }

    private fun getSyncingLiveData(projectId: String) =
        appState.get("$KEY_PREFIX_SYNCING:$projectId", MutableLiveData(false))

    private fun getSyncErrorLiveData(projectId: String) =
        appState.get("$KEY_PREFIX_ERROR:$projectId", MutableLiveData<FormSourceException>(null))

    private fun getDiskErrorLiveData(projectId: String): MutableLiveData<String?> =
        appState.get("diskError:$projectId", MutableLiveData<String?>(null))

    companion object {
        const val KEY_PREFIX_SYNCING = "syncStatusSyncing"
        const val KEY_PREFIX_ERROR = "syncStatusError"
    }
}

private fun formDownloader(projectDependencyProvider: ProjectDependencyProvider, clock: Supplier<Long>): ServerFormDownloader {
    return ServerFormDownloader(
        projectDependencyProvider.formSource,
        projectDependencyProvider.formsRepository,
        File(projectDependencyProvider.cacheDir),
        projectDependencyProvider.formsDir,
        FormMetadataParser(),
        clock
    )
}

private fun serverFormsDetailsFetcher(
    projectDependencyProvider: ProjectDependencyProvider,
    diskFormsSynchronizer: FormsDirDiskFormsSynchronizer
): ServerFormsDetailsFetcher {
    return ServerFormsDetailsFetcher(
        projectDependencyProvider.formsRepository,
        projectDependencyProvider.formSource,
        diskFormsSynchronizer
    )
}

private fun diskFormsSynchronizer(projectDependencyProvider: ProjectDependencyProvider): FormsDirDiskFormsSynchronizer {
    return FormsDirDiskFormsSynchronizer(
        projectDependencyProvider.formsRepository,
        projectDependencyProvider.formsDir
    )
}
