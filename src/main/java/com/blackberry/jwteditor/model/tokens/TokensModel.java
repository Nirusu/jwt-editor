/*
Author : Dolph Flynn

Copyright 2025 Dolph Flynn

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

package com.blackberry.jwteditor.model.tokens;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableList;

public class TokensModel implements TokenRepository {
    private final List<Token> tokens;
    private final Object lock;
    private final List<TokensModelListener> modelListeners;

    public TokensModel() {
        this.tokens = new ArrayList<>();
        this.lock = new Object();
        this.modelListeners = new CopyOnWriteArrayList<>();
    }

    public List<Token> tokens() {
        synchronized (lock) {
            return unmodifiableList(tokens);
        }
    }

    public void addTokensModelListener(TokensModelListener modelListener) {
        this.modelListeners.add(modelListener);
    }

    @Override
    public void add(Token token) {
        synchronized (lock) {
            tokens.add(token);
        }

        for (TokensModelListener modelListener : modelListeners) {
            modelListener.notifyTokenInserted(token);
        }
    }
}
