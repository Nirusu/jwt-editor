/*
Author : Fraser Winterborn

Copyright 2021 BlackBerry Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.blackberry.jwteditor.view.dialog.keys;

import com.blackberry.jwteditor.exceptions.UnsupportedKeyException;
import com.blackberry.jwteditor.model.keys.JWKKeyFactory;
import com.blackberry.jwteditor.model.keys.Key;
import com.blackberry.jwteditor.presenter.PresenterStore;
import com.blackberry.jwteditor.utils.JSONUtils;
import com.blackberry.jwteditor.utils.Utils;
import com.blackberry.jwteditor.view.rsta.RstaFactory;
import com.blackberry.jwteditor.view.utils.DocumentAdapter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.UUID;

/**
 * "New Symmetric Key" dialog for Keys tab
 */
public class SymmetricKeyDialog extends KeyDialog {
    private static final String TITLE_RESOURCE_ID = "keys_new_title_symmetric";

    private final RstaFactory rstaFactory;
    private final Color textAreaKeyInitialBackgroundColor;
    private final Color textAreaKeyInitialCurrentLineHighlightColor;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner spinnerKeySize;
    private JButton buttonGenerate;
    private RSyntaxTextArea textAreaKey;
    private JLabel labelError;

    private OctetSequenceKey jwk;

    public SymmetricKeyDialog(Window parent, PresenterStore presenters, RstaFactory rstaFactory, OctetSequenceKey jwk) {
        super(parent, TITLE_RESOURCE_ID);

        this.rstaFactory = rstaFactory;
        this.presenters = presenters;
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        spinnerKeySize.setModel(new SpinnerNumberModel(128, 0, null, 8));

        spinnerKeySize.addChangeListener(e -> {
            int value = (int) spinnerKeySize.getValue();

            // Round any manually entered values up to the nearest number of bytes
            if (value == 0 || value % 8 > 0) {
                int nextByteSize = ((value / 8) + 1) * 8;
                spinnerKeySize.setValue(nextByteSize);
            }
        });

        // Attach event listeners for Generate button and text entry changing
        buttonGenerate.addActionListener(e -> generate());

        DocumentListener documentListener = new DocumentAdapter(e -> checkInput());
        textAreaKey.getDocument().addDocumentListener(documentListener);

        textAreaKeyInitialBackgroundColor = textAreaKey.getBackground();
        textAreaKeyInitialCurrentLineHighlightColor = textAreaKey.getCurrentLineHighlightColor();

        // Set the key id and key value fields if provided
        if (jwk != null) {
            originalId = jwk.getKeyID();
            textAreaKey.setText(JSONUtils.prettyPrintJSON(jwk.toJSONString()));
            spinnerKeySize.setValue(jwk.size());
        }
    }

    /**
     * Event handler called when form input changes
     */
    private void checkInput() {
        // Clear the error state. Disable OK while parsing
        textAreaKey.setBackground(textAreaKeyInitialBackgroundColor);
        textAreaKey.setCurrentLineHighlightColor(textAreaKeyInitialCurrentLineHighlightColor);
        buttonOK.setEnabled(false);
        labelError.setText(" ");
        jwk = null;

        // If there is a text in the text entry
        if(textAreaKey.getText().length() > 0){
            try {
                // Try to parse as a symmetric key JWK
                OctetSequenceKey octetSequenceKey = OctetSequenceKey.parse(textAreaKey.getText());

                // Check the JWK contains a 'kid' value, set form to error mode if not
                if(octetSequenceKey.getKeyID() == null){
                    textAreaKey.setBackground(Color.PINK);
                    textAreaKey.setCurrentLineHighlightColor(Color.PINK);
                    labelError.setText(Utils.getResourceString("error_missing_kid"));
                }
                else {
                    // No errors, enable the OK button
                    buttonOK.setEnabled(true);
                    jwk = octetSequenceKey;
                }

            } catch (ParseException e) {
                // Set form to error mode if JWK parsing fails
                textAreaKey.setBackground(Color.PINK);
                textAreaKey.setCurrentLineHighlightColor(Color.PINK);
                labelError.setText(Utils.getResourceString("error_invalid_key"));
            }
        }
    }

    /**
     * Event handler for the generate button
     */
    private void generate() {
        // Generate a random 'kid'
        String keyId = UUID.randomUUID().toString();

        // Generate a new symmetric key based on the key size selected in the combobox
        byte[] key = new byte[(int) spinnerKeySize.getValue() / 8];
        SecureRandom rng = new SecureRandom();
        rng.nextBytes(key);

        // Create the OctetSequenceKey from the bytes
        // Use the Builder directly to skip length checks - the spinner model enforces these
        OctetSequenceKey octetSequenceKey = new OctetSequenceKey.Builder(key).keyID(keyId).build();

        // Set the text area contents to the JSON form of the newly generated key
        textAreaKey.setText(JSONUtils.prettyPrintJSON(octetSequenceKey.toJSONString()));
    }

    /**
     * Get the new/modified key resulting from the operations of this dialog
     * @return the new/modified JWK
     */
    public Key getKey() {
        try {
            return jwk == null ? null : JWKKeyFactory.from(jwk);
        } catch (UnsupportedKeyException ignored) {
            return null;
        }
    }

    /**
     * Called when the Cancel or X button is pressed. Set the changed key to null and destroy the window
     */
    @Override
    void onCancel() {
        jwk = null;
        dispose();
    }

    private void createUIComponents() {
        textAreaKey = rstaFactory.buildDefaultTextArea();
    }
}
