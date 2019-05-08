/*
 * Copyright 2017 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudiator.messaging;

import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;

/**
 * Created by daniel on 24.05.17.
 */
public interface ResponseCallback<T> {

  static ResponseCallback<?> empty() {
    return new ResponseCallback<Object>() {
      @Override
      public void accept(@Nullable Object content, @Nullable Error error) {
        //intentionally left empty
      }
    };
  }

  public void accept(@Nullable T content, @Nullable Error error);

}
