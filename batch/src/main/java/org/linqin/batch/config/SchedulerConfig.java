package org.linqin.batch.config;

import jakarta.annotation.Resource;

public class SchedulerConfig {
    @Resource
    private MyJobFactory myJobFactory;
}
