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
package org.deephacks.westty.jaxrs;

/**
 * This object will be contained in the response whenever an exception
 * occured from invoking the JAXRS endpoint.
 */
public class JaxrsConfigError {
    private String module = "";
    private int code = 0;
    private String message = "";

    public JaxrsConfigError() {
    }

    public JaxrsConfigError(String msg) {
        this.message = msg;
    }

    public JaxrsConfigError(String module, int code, String message) {
        this.module = module;
        this.code = code;
        this.message = message;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModule() {
        return module;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
