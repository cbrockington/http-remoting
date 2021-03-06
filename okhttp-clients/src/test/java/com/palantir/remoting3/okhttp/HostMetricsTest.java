/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.remoting3.okhttp;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public final class HostMetricsTest {

    private static final String SERVICE_NAME = "serviceName";
    private static final String HOSTNAME = "hostname";

    private DefaultHostMetrics hostMetrics;

    @Before
    public void before() {
        hostMetrics = new DefaultHostMetrics(SERVICE_NAME, HOSTNAME);
    }

    @Test
    public void testUpdateMetricUpdatesMeter() {
        Map<Integer, Timer> testCases = ImmutableMap.<Integer, Timer>builder()
                .put(100, hostMetrics.get1xx())
                .put(200, hostMetrics.get2xx())
                .put(300, hostMetrics.get3xx())
                .put(400, hostMetrics.get4xx())
                .put(500, hostMetrics.get5xx())
                .put(600, hostMetrics.getOther())
                .build();

        for (Map.Entry<Integer, Timer> testCase : testCases.entrySet()) {
            Timer timer = testCase.getValue();
            assertThat(timer.getCount()).isZero();
            assertThat(timer.getSnapshot().getMin()).isEqualTo(0);

            hostMetrics.record(testCase.getKey(), 1);

            assertThat(timer.getCount()).isEqualTo(1);
            assertThat(timer.getSnapshot().getMin()).isEqualTo(1_000);
        }
    }

    @Test
    public void testIoExceptionUpdatesMeter() {
        Meter ioExceptions = hostMetrics.getIoExceptions();
        assertThat(ioExceptions.getCount()).isZero();

        hostMetrics.recordIoException();

        assertThat(ioExceptions.getCount()).isEqualTo(1);
    }
}
