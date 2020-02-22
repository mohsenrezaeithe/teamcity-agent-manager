/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
 */

package mrtc.agentManagerListener;

import java.util.*;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerListener;
import jetbrains.buildServer.serverSide.agentPools.AgentPool;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager;
import org.jetbrains.annotations.NotNull;


/**
 * Authorizes agents as soon as they're registered
 */
public class AutoAuthorizeListener extends BuildServerAdapter {
  private final static Logger LOG = Logger.getInstance(ServerListener.class.getName());
  private final SBuildServer myBuildServer;
  private final AgentPoolManager myAgentPoolManager;

  public AutoAuthorizeListener(SBuildServer sBuildServer, AgentPoolManager agentPoolManager) {
    myBuildServer = sBuildServer;
    myAgentPoolManager = agentPoolManager;
  }

  public void register() {
    myBuildServer.addListener(this);
  }

  @Override
  public void agentRegistered(@NotNull SBuildAgent sBuildAgent, long l) {
    Map<String,String> parameters = sBuildAgent.getAvailableParameters();
    if ((parameters.containsKey("autoAuthorize") && parameters.get("autoAuthorize").equals("true")) ||
        (parameters.containsKey("autoManage") && parameters.get("autoManage").equals("true"))) {
      moveAgentToPool(sBuildAgent);
      sBuildAgent.setAuthorized(true, null, "ASG agent registered");
    }
  }

  @Override
  public void agentUnregistered(@NotNull SBuildAgent sBuildAgent) {
    Map<String,String> parameters = sBuildAgent.getAvailableParameters();
    if ((parameters.containsKey("autoAuthorize") && parameters.get("autoAuthorize").equals("true")) ||
        (parameters.containsKey("autoManage") && parameters.get("autoManage").equals("true"))) {
      sBuildAgent.setAuthorized(false, null, "ASG agent unregistered");
    }
  }
  
  private void moveAgentToPool(@NotNull SBuildAgent sBuildAgent) {
    Map<String,String> parameters = sBuildAgent.getAvailableParameters();

    if (!parameters.containsKey("agentPool")) {
      return;
    }

    final String agentPoolName = parameters.get("agentPool");
    AgentPool agentPool = myAgentPoolManager.getAllAgentPools().stream()
            .filter(pool -> pool.getName().equalsIgnoreCase(agentPoolName))
            .findFirst().orElse(null);

    if (agentPool == null) {
      LOG.warn(String.format("Agent Pool '%s' could not be found. Agent will be registered in the default pool", agentPool));
      return;
    }

    try {
      myAgentPoolManager.moveAgentTypesToPool(agentPool.getAgentPoolId(), Collections.singleton(sBuildAgent.getAgentTypeId()));
    } catch (Exception e) {
      LOG.error("Error assigning an agent to pool. Agent will be registered in the default pool " + e.toString());
    }
  }
}
