/*
 *  Copyright (2020) Subhabrata Ghosh (subho dot ghosh at outlook dot com)
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

package com.codekutter.r2db.driver.model;

import com.codekutter.common.model.IKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

@Getter
@Setter
@ToString
@Accessors(fluent = true)
public class S3FileKey implements IKey, Comparable<S3FileKey> {
    private String bucket;
    private String key;


    @Override
    public int compareTo(@Nonnull S3FileKey s3FileKey) {
        int ret = bucket.compareTo(s3FileKey.bucket);
        if (ret == 0) {
            ret = key.compareTo(s3FileKey.key);
        }
        return ret;
    }

    /**
     * Get the String representation of the key.
     *
     * @return - Key String
     */
    @Override
    public String stringKey() {
        return String.format("%s::%s", bucket, key);
    }

    /**
     * Compare the current key to the target.
     *
     * @param key - Key to compare to
     * @return - == 0, < -x, > +x
     */
    @Override
    public int compareTo(IKey key) {
        if (key instanceof S3FileKey) {
            S3FileKey fk = (S3FileKey)key;
            int ret = bucket.compareTo(fk.bucket);
            if (ret == 0) {
                ret = this.key.compareTo(fk.key);
            }
            return ret;
        }
        return -1;
    }
}
