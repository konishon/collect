package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.tasks.SaveResult;

import static org.odk.collect.android.tasks.SaveToDiskTask.SAVED;
import static org.odk.collect.android.tasks.SaveToDiskTask.SAVED_AND_EXIT;
import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class FormEntryViewModel extends ViewModel {

    private final MutableLiveData<Boolean> requiresReasonToContinue = new MutableLiveData<>(false);

    @Nullable
    private MutableLiveData<SaveRequest> saveRequest;

    @Nullable
    private AuditEventLogger auditEventLogger;

    @Nullable
    private String reason;

    @Nullable
    private SaveRequest lastSaveRequest;

    public LiveData<Boolean> requiresReasonToContinue() {
        return requiresReasonToContinue;
    }

    public void setAuditEventLogger(@Nullable AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
    }

    public LiveData<SaveRequest> saveForm(boolean complete, String updatedSaveName, boolean viewExiting) {
        saveRequest = new MutableLiveData<>(null);
        lastSaveRequest = new SaveRequest(complete, updatedSaveName, viewExiting);

        if (!requiresReasonToSave()) {
            lastSaveRequest.setState(SaveRequest.State.SAVING);
        } else {
            lastSaveRequest.setState(SaveRequest.State.CHANGE_REASON_REQUIRED);
            requiresReasonToContinue.setValue(true);
        }

        saveRequest.setValue(lastSaveRequest);
        return saveRequest;
    }

    public void saveToDiskTaskComplete(SaveResult saveResult, long currentTime) {
        switch (saveResult.getSaveResult()) {
            case SAVED:
            case SAVED_AND_EXIT: {
                if (auditEventLogger != null) {
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_SAVE, false, currentTime);

                    if (lastSaveRequest.isViewExiting()) {
                        if (lastSaveRequest.isFormComplete()) {
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_EXIT, false, currentTime);
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true, currentTime);
                        } else {
                            auditEventLogger.logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, currentTime);
                        }
                    }
                }

                lastSaveRequest.setState(SaveRequest.State.SAVED);
                saveRequest.setValue(lastSaveRequest);
                break;
            }
        }
    }

    public void editingForm() {
        if (auditEventLogger != null) {
            auditEventLogger.setEditing(true);
        }
    }

    public void promptDismissed() {
        requiresReasonToContinue.setValue(false);
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void saveReason(Long currentTime) {
        if (reason != null && !isBlank(reason)) {
            if (auditEventLogger != null) {
                auditEventLogger.logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, currentTime, reason);
            }

            requiresReasonToContinue.setValue(false);

            if (saveRequest != null) {
                lastSaveRequest.setState(SaveRequest.State.SAVING);
                saveRequest.setValue(lastSaveRequest);
            }
        }
    }

    public String getReason() {
        return reason;
    }

    private boolean requiresReasonToSave() {
        return auditEventLogger != null
                && auditEventLogger.isEditing()
                && auditEventLogger.isChangeReasonRequired()
                && auditEventLogger.isChangesMade();
    }

    public static class SaveRequest {

        private final boolean formComplete;
        private final String updatedSaveName;
        private final boolean viewExiting;

        private State state;

        public SaveRequest(boolean formComplete, String updatedSaveName, boolean viewExiting) {
            this.formComplete = formComplete;
            this.updatedSaveName = updatedSaveName;
            this.viewExiting = viewExiting;
        }

        public boolean isFormComplete() {
            return formComplete;
        }

        public String getUpdatedSaveName() {
            return updatedSaveName;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public boolean isViewExiting() {
            return viewExiting;
        }

        public enum State {
            CHANGE_REASON_REQUIRED,
            SAVING,
            SAVED
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel();
        }
    }
}
