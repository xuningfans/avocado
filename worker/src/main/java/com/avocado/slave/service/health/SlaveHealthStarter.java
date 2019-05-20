package com.avocado.slave.service.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * SlaveHealthExecuter class
 *
 * @author xuning
 * @date 2019-05-06 15:33
 */
@Slf4j
@Component
public class SlaveHealthStarter {

    @Resource
    private SlaveHealthWorker slaveHealthWorker;

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> slaveHealthWorker.setFlag(false)));

        slaveHealthWorker.execute();
    }

}
