/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.westty.example;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "EXAMPLE")
public class ExampleEntity {
    @Id
    @Column(name = "UUID")
    private String id;
    @Column(name = "PARAM")
    private String param;

    public ExampleEntity() {
    }

    public ExampleEntity(String id, String param) {
        if (id == null || id.trim().equals("")) {
            this.id = UUID.randomUUID().toString();
        } else {
            this.id = id;
        }
        this.param = param;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(ExampleEntity.class).add("id", id).add("param", param)
                .toString();
    }
}