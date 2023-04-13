/*
Author : Dolph Flynn

Copyright 2022 Dolph Flynn

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

package com.blackberry.jwteditor.view.editor;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;
import com.blackberry.jwteditor.presenter.PresenterStore;
import com.blackberry.jwteditor.view.hexcodearea.HexCodeAreaFactory;
import com.blackberry.jwteditor.view.rsta.RstaFactory;

import static burp.api.montoya.internal.ObjectFactoryLocator.FACTORY;

public class ResponseEditorView extends EditorView implements ExtensionProvidedHttpResponseEditor {
    public ResponseEditorView(PresenterStore presenters, RstaFactory rstaFactory, Logging logging, boolean editable) {
        super(presenters, rstaFactory, new HexCodeAreaFactory(logging), editable);
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) {
        HttpResponse httpResponse = requestResponse.response();
        presenter.setMessage(httpResponse.toByteArray().toString());
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse) {
        String content = requestResponse.response().toByteArray().toString();
        return presenter.isEnabled(content);
    }

    @Override
    public HttpResponse getResponse() {
        return FACTORY.httpResponse(presenter.getMessage());
    }
}
