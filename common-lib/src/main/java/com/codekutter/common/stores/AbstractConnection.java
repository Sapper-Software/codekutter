/*
 *  Copyright (2019) Subhabrata Ghosh (subho dot ghosh at outlook dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.codekutter.common.stores;

import com.codekutter.common.model.IEntity;
import com.codekutter.zconfig.common.IConfigurable;
import com.codekutter.zconfig.common.model.annotations.ConfigAttribute;
import com.codekutter.zconfig.common.model.annotations.ConfigPath;
import com.codekutter.zconfig.common.model.annotations.ConfigValue;
import com.codekutter.zconfig.common.transformers.ClassListParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
@ConfigPath(path = "connection")
@SuppressWarnings("rawtypes")
public abstract class AbstractConnection<T> implements IConfigurable, Closeable {
    @ConfigAttribute(name = "name")
    private String name;
    @ConfigAttribute(name = "class")
    private Class<? extends AbstractConnection> type;
    @ConfigAttribute(name = "source")
    private EConfigSource configSource = EConfigSource.File;
    @Setter(AccessLevel.NONE)
    private final ConnectionState state = new ConnectionState();
    @ConfigValue(name = "classes", parser = ClassListParser.class)
    private List<Class<?>> supportedTypes = new ArrayList<>();

    public AbstractConnection() {

    }

    public void addSupportedType(@Nonnull Class<? extends IEntity> type) {
        supportedTypes.add(type);
    }

    public abstract T connection() throws ConnectionException;

    public abstract boolean hasTransactionSupport();

    public abstract void close(@Nonnull T connection) throws ConnectionException;
}
