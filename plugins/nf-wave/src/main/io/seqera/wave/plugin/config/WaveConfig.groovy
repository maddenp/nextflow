/*
 * Copyright 2020-2022, Seqera Labs
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
 *
 */

package io.seqera.wave.plugin.config

import groovy.transform.CompileStatic
import nextflow.util.Duration

/**
 * Model Wave client configuration
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class WaveConfig {
    final private static String DEF_ENDPOINT = 'http://localhost:9090'
    final private Boolean enabled
    final private String endpoint
    final private List<URL> containerConfigUrl
    final private Duration tokensCacheMaxDuration
    final private MambaOpts mambaOpts

    WaveConfig(Map opts, Map<String,String> env=System.getenv()) {
        this.enabled = opts.enabled
        this.endpoint = (opts.endpoint?.toString() ?: env.get('WAVE_API_ENDPOINT') ?: DEF_ENDPOINT)?.stripEnd('/')
        this.containerConfigUrl = parseConfig(opts, env)
        this.tokensCacheMaxDuration = opts.navigate('tokens.cache.maxDuration', '15m') as Duration
        if( !endpoint.startsWith('http://') && !endpoint.startsWith('https://') )
            throw new IllegalArgumentException("Endpoint URL should start with 'http:' or 'https:' protocol prefix - offending value: $endpoint")
        this.mambaOpts = opts.navigate('build.mamba', Collections.emptyMap()) as MambaOpts
    }

    Boolean enabled() { this.enabled }

    String endpoint() { this.endpoint }

    MambaOpts mambaOpts() { this.mambaOpts }

    protected List<URL> parseConfig(Map opts, Map<String,String> env) {
        List<String> result = new ArrayList<>(10)
        if( !opts.containerConfigUrl && env.get('WAVE_CONTAINER_CONFIG_URL') ) {
            result.add(checkUrl(env.get('WAVE_CONTAINER_CONFIG_URL')))
        }
        else if( opts.containerConfigUrl instanceof CharSequence ) {
            result.add(checkUrl(opts.containerConfigUrl.toString()))
        }
        else if( opts.containerConfigUrl instanceof List ) {
            for( def it : opts.containerConfigUrl ) {
                result.add(checkUrl(it.toString()))
            }
        }

        return result.collect(it -> new URL(it))
    }

    private String checkUrl(String value) {
        if( value && (!value.startsWith('http://') && !value.startsWith('https://')))
            throw new IllegalArgumentException("Wave container config URL should start with 'http:' or 'https:' protocol prefix - offending value: $value")
        return value
    }

    List<URL> containerConfigUrl() {
        return containerConfigUrl
    }

    Duration tokensCacheMaxDuration() { 
        return tokensCacheMaxDuration 
    }
    
}