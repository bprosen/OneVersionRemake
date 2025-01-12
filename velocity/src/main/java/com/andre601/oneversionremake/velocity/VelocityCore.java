/*
 * Copyright 2020 - 2021 Andre601
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.andre601.oneversionremake.velocity;

import com.andre601.oneversionremake.core.OneVersionRemake;
import com.andre601.oneversionremake.core.Parser;
import com.andre601.oneversionremake.core.commands.CommandHandler;
import com.andre601.oneversionremake.core.proxy.ProtocolVersionResolver;
import com.andre601.oneversionremake.core.proxy.ProxyPlatform;
import com.andre601.oneversionremake.core.files.ConfigHandler;
import com.andre601.oneversionremake.core.interfaces.PluginCore;
import com.andre601.oneversionremake.core.interfaces.ProxyLogger;
import com.andre601.oneversionremake.velocity.commands.CmdOneVersionRemake;
import com.andre601.oneversionremake.velocity.listener.VelocityLoginListener;
import com.andre601.oneversionremake.velocity.logging.VelocityLogger;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import org.bstats.charts.DrilldownPie;
import org.bstats.velocity.Metrics;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class VelocityCore implements PluginCore{
    
    private final ProxyLogger logger;
    private final ProxyServer proxy;
    private final Path path;
    
    private final Metrics.Factory factory;
    
    private OneVersionRemake core;
    
    @Inject
    public VelocityCore(ProxyServer proxy, @DataDirectory Path path, Metrics.Factory factory){
        this.logger = new VelocityLogger(LoggerFactory.getLogger("OneVersionRemake"));
        
        this.proxy = proxy;
        this.path = path;
        this.factory = factory;
    }
    
    @Subscribe
    public void initialize(ProxyInitializeEvent event){
        this.core = new OneVersionRemake(this);
    }
    
    // PluginCore stuff
    
    @Override
    public void loadCommands(){
        CommandMeta commandMeta = getProxy().getCommandManager()
                .metaBuilder("oneversionremake")
                .aliases("ovr")
                .build();
        
        getProxy().getCommandManager().register(commandMeta, new CmdOneVersionRemake(this));
    }
    
    @Override
    public void loadEventListeners(){
        new VelocityLoginListener(this);
    }
    
    @Override
    public void loadMetrics(){
        Metrics metrics = factory.make(this, 10341);
        
        metrics.addCustomChart(new DrilldownPie("allowed_protocols", () -> core.getPieMap()));
        
    }
    
    @Override
    public Path getPath(){
        return path;
    }
    
    @Override
    public ProxyPlatform getProxyPlatform(){
        return ProxyPlatform.VELOCITY;
    }
    
    @Override
    public ProxyLogger getProxyLogger(){
        return logger;
    }
    
    @Override
    public ConfigHandler getConfigHandler(){
        return core.getConfigHandler();
    }
    
    @Override
    public ProtocolVersionResolver getProtocolVersionResolver(){
        return core.getProtocolVersionResolver();
    }
    
    @Override
    public CommandHandler getCommandHandler(){
        return core.getCommandHandler();
    }
    
    @Override
    public Parser getComponentParser(){
        return core.getComponentParser();
    }
    
    @Override
    public String getVersion(){
        return core.getVersion();
    }
    
    @Override
    public String getProxyVersion(){
        return getProxy().getVersion().getVersion();
    }
    
    public ProxyServer getProxy(){
        return proxy;
    }
    
    public ServerPing.SamplePlayer[] getPlayers(List<String> lines, List<Integer> serverProtocols, int userProtocol, boolean majorOnly){
        return core.getPlayers(ServerPing.SamplePlayer.class, lines, serverProtocols, userProtocol, majorOnly)
                .toArray(new ServerPing.SamplePlayer[0]);
    }
}
