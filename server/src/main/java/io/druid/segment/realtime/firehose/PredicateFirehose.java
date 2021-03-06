/*
 * Druid - a distributed column store.
 * Copyright 2012 - 2015 Metamarkets Group Inc.
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

package io.druid.segment.realtime.firehose;

import com.google.common.base.Predicate;
import io.druid.data.input.Firehose;
import io.druid.data.input.InputRow;

import java.io.IOException;

/**
 * Provides a view on a firehose that only returns rows that match a certain predicate.
 * Not thread-safe.
 */
public class PredicateFirehose implements Firehose
{
  private final Firehose firehose;
  private final Predicate<InputRow> predicate;

  private InputRow savedInputRow = null;

  public PredicateFirehose(Firehose firehose, Predicate<InputRow> predicate)
  {
    this.firehose = firehose;
    this.predicate = predicate;
  }

  @Override
  public boolean hasMore()
  {
    if (savedInputRow != null) {
      return true;
    }

    while (firehose.hasMore()) {
      final InputRow row = firehose.nextRow();
      if (predicate.apply(row)) {
        savedInputRow = row;
        return true;
      }
    }

    return false;
  }

  @Override
  public InputRow nextRow()
  {
    final InputRow row = savedInputRow;
    savedInputRow = null;
    return row;
  }

  @Override
  public Runnable commit()
  {
    return firehose.commit();
  }

  @Override
  public void close() throws IOException
  {
    firehose.close();
  }
}
