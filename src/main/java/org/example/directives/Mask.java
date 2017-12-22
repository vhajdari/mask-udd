/*
 *  Copyright © 2017 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package org.example.directives;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.cdap.api.common.Bytes;
import co.cask.wrangler.api.*;
import co.cask.wrangler.api.parser.*;
import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.text.MaskFormatter;
import java.util.List;

/**
 * This directive scrambles the input string using a reversible cipher.
 */
@Plugin(type = Directive.Type)
@Name(Mask.NAME)
@Description("Scrambles the input string using a reversible cipher.")
public final class Mask implements Directive {
    public static final String NAME = "mask-scramble";
    private String column;
    private String codec;
    private Number shift;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
        builder.define("column", TokenType.COLUMN_NAME);
        builder.define("cipher", TokenType.IDENTIFIER);
        builder.define("shift", TokenType.NUMERIC, Optional.TRUE);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.column = ((ColumnName) args.value("column")).value();
        this.codec = ((Identifier) args.value("cipher")).value();
        this.shift = ((Numeric) args.value("shift")).value();
        if (!codec.equalsIgnoreCase("rot13") && !codec.equalsIgnoreCase("ceasar"))
        {
            throw new DirectiveParseException(
                    String.format("Invalid option '%s' specified. The allowed options are ceasar or rot13.", codec)
            );
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context)
            throws DirectiveExecutionException, ErrorRowException {
        for (Row row : rows) {
            int idx = row.find(column);
            int s = shift.intValue();
            if (idx != -1) {
                Object object = row.getValue(idx);

                switch (codec) {
                    case "ceasar": {
                        if (object instanceof String) {
                            if (object != null) {
                                String value = (String) object;
                                StringBuffer masked = CaesarCipher.scramble(value, s);
                                row.setValue(idx, masked);
                            }
                        } else if (object instanceof byte[]) {
                            String value = Bytes.toString((byte[]) object);
                            String masked = CaesarCipher.scramble(value, s).toString();
                            row.setValue(idx, masked);
                        }
                    }
                    break;

                    case "rot13": {
                        if (object instanceof String) {
                            if (object != null) {
                                String value = (String) object;
                                String masked = Rot13.rotate(value);
                                row.setValue(idx, masked);
                            }
                        } else if (object instanceof byte[]) {
                            String value = Bytes.toString((byte[]) object);
                            String masked = Rot13.rotate(value);
                            row.setValue(idx, masked);
                        }
                    }
                    break;

                }
            }

        }
        return rows;
    }

    @Override
    public void destroy() {
        // no-op
    }
}
