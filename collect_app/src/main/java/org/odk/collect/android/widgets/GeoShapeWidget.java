/*
 * Copyright (C) 2014 GeoODK
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * GeoShapeWidget is the widget that allows the user to get Collect multiple GPS points.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class GeoShapeWidget extends BaseGeoWidget implements BinaryWidget {

    public GeoShapeWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);

        startGeoButton = getSimpleButton(getContext().getString(R.string.get_shape));

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(startGeoButton);
        answerLayout.addView(answerDisplay);
        addAnswerView(answerLayout);

        boolean dataAvailable = false;
        String s = questionDetails.getPrompt().getAnswerText();
        if (s != null && !s.equals("")) {
            dataAvailable = true;
            setBinaryData(s);
        }

        updateButtonLabelsAndVisibility(dataAvailable);
    }

    protected void startGeoActivity() {
        Intent intent = new Intent(getContext(), GeoPolyActivity.class)
            .putExtra(GeoPolyActivity.ANSWER_KEY, answerDisplay.getText().toString())
            .putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE);
        ((Activity) getContext()).startActivityForResult(intent, RequestCodes.GEOSHAPE_CAPTURE);
    }

    protected void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        startGeoButton.setText(dataAvailable ? R.string.geoshape_view_change_location : R.string.get_shape);
    }

    @Override
    public void setBinaryData(Object answer) {
        String answerText = answer.toString();
        answerDisplay.setText(answerText);
        updateButtonLabelsAndVisibility(!answerText.isEmpty());
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerDisplay.getText().toString();

        return !s.isEmpty()
                ? new StringData(s)
                : null;
    }
}
