/*
Author : Dolph Flynn

Copyright 2023 Dolph Flynn

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

package com.blackberry.jwteditor.view.tokens;

import com.blackberry.jwteditor.model.jose.JWS;
import com.blackberry.jwteditor.view.tokens.TokensTableColumnConfiguration.TokensTableColumns;
import com.blackberry.jwteditor.view.utils.table.GenericTableModel;

import java.util.ArrayList;
import java.util.List;

class TokensTableModel extends GenericTableModel {
    private final List<JWS> data;

    TokensTableModel(Iterable<JWS> keys) {
        super(new TokensTableColumnConfiguration());

        this.data = new ArrayList<>();
        keys.forEach(data::add);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.size()) {
            return null;
        }

        JWS token = data.get(rowIndex);
        TokensTableColumns column = TokensTableColumns.fromIndex(columnIndex);

        return switch (column) {
            case ID -> "";
            case HOST -> "";
            case PATH -> "";
            case ALGORITHM -> "";
            case KEY_ID -> "";
        };
    }
}
