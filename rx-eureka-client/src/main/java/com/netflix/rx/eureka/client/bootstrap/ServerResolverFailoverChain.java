/*
 * Copyright 2014 Netflix, Inc.
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

package com.netflix.rx.eureka.client.bootstrap;

import java.net.SocketAddress;

import com.netflix.rx.eureka.client.ServerResolver;
import rx.Observable;
import rx.functions.Func1;

/**
 * Provide means to use multiple sources of bootstrap server list.
 *
 * @author Tomasz Bak
 */
public class ServerResolverFailoverChain<A extends SocketAddress> implements ServerResolver<A> {

    private final Observable<ServerEntry<A>> servers;
    private final ServerResolver<A>[] resolvers;

    @SafeVarargs
    public ServerResolverFailoverChain(ServerResolver<A>... resolvers) {
        this.resolvers = resolvers;
        Observable<ServerEntry<A>> chain = null;
        for (final ServerResolver<A> resolver : resolvers) {
            if (null == chain) {
                chain = resolver.resolve();
            } else {
                chain = chain.onErrorResumeNext(new Func1<Throwable, Observable<? extends ServerEntry<A>>>() {
                    @Override
                    public Observable<? extends ServerEntry<A>> call(Throwable throwable) {
                        return resolver.resolve();
                    }
                });
            }
        }

        servers = chain;
    }

    @Override
    public Observable<ServerEntry<A>> resolve() {
        return servers;
    }
}
