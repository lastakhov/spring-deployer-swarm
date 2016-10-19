package org.springframework.cloud.deployer.spi.swarm;

import com.spotify.docker.client.messages.swarm.ContainerStatus;
import com.spotify.docker.client.messages.swarm.Task;
import com.spotify.docker.client.messages.swarm.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.deployer.spi.app.AppInstanceStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joriscaloud on 13/10/16.
 */
public class SwarmAppInstanceStatus implements AppInstanceStatus {

    private static Logger logger = LoggerFactory.getLogger(SwarmAppInstanceStatus.class);
    private SwarmDeployerProperties properties;
    private TaskStatus taskStatus;
    private ContainerStatus containerStatus;
    private Task task;
    private String appId;

    public SwarmAppInstanceStatus(SwarmDeployerProperties properties, String appId) {
        this.properties = properties;
        this.appId = appId;
        this.task = task;
        this.taskStatus = task.status();
        this.containerStatus = taskStatus.containerStatus();
    }
    public Task getTaskFromAppId(String appId) {

    }

    public String getId() {
        return String.format("%s:%s", containerStatus.containerID());
    }

    public DeploymentState getState() {
        return taskStatus != null ? mapState() : DeploymentState.unknown;
    }


    /**
     * Maps SWARM phases/states onto Spring Cloud Deployer states
     */
    private DeploymentState mapState() {
        logger.debug("{} - ContainerStatus [ {} ]", taskStatus.containerStatus());
        switch (taskStatus.state()) {

            case TaskStatus.TASK_STATE_PENDING:
                return DeploymentState.deploying;

            case  TaskStatus.TASK_STATE_FAILED:
                return DeploymentState.failed;

            case TaskStatus.TASK_STATE_REJECTED:
                return DeploymentState.error;

            case TaskStatus.TASK_STATE_SHUTDOWN:
                return DeploymentState.undeployed;

            case TaskStatus.TASK_STATE_COMPLETE:
                return DeploymentState.deployed;

            default:
                return DeploymentState.unknown;
        }
    }

    public Map<String, String> getAttributes() {
        Map<String, String> result = new HashMap<>();

        if (containerStatus != null) {
            result.put("container_id", containerStatus.containerID());

        }
;
        if (taskStatus != null && containerStatus.exitCode() != null) {
            result.put("container_last_termination_exit_code", "" + containerStatus.exitCode());
        }
        return result;
    }

}