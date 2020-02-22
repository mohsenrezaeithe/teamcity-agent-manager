# TeamCity Agent Manager

*This is a rework of and extention to the [AutoAuth plugin](https://github.com/FLGMwt/team-city-agent-auto-auth)*.

A TeamCity server plugin to automatically manager build agents on events captured by the server. This plugin helps automating the life cycle of build agents in the infrastructure, eliminating the need to manually authorize, unauthorize, and/or cleanup build agents. An appealing use case for this life cycle management would be the use of [Cloud Profiles](https://www.jetbrains.com/help/teamcity/agent-cloud-profile.html) for automatically scaling agent pools based on workload.

**NOTE:** This plugin eliminates the need for manually authorizing, unauthorizing, and cleaning up TeamCity agents with the server, hence, it is important that this configuration is only used with a TeamCity server that is only exposed to trusted environments and agents. 

# Distribution

All distributions of this plugin can be found on the [JetBrain's Plugin portal](https://plugins.jetbrains.com/plugin/13105-agent-auto-add-remove).

# Build

Run `ant dist` to create a dist for using with a TeamCity server distribution. Requires `teamcity.distribution` and `teamcity.data.directory` path variables.

# Agent Configuration

The following parameters can be used in the [build agent configuration](https://www.jetbrains.com/help/teamcity/build-agent-configuration.html) to test the functionality with the server:
  1. `autoAuthorize=true` for authorizing and unauthorizing agents on registration and unregistration events.
  1. `autoManage=true` for cleaning up agents in addition to authorization and unauthorization.
  1. `agentPool=<poolName>` for specifing a non-default agent pool to join on auto-authorization event.

# Server Configuration

In a testing environment:

1. Upload `AgentAutoAddRemove.zip` to the TeamCity server plugin directory (or from the UI at `Administration` → `Plugins List`).
1. Reload or restart the TeamCity server.
1. Add the right agent parameter to the configuration in `$agentDir/conf/buildAgent.properties`.
1. Start the build agent.
1. On registration with the server, the agent is authorized.
1. On unregistration with the server, the agent is unauthorized or removed from the agent list.

