/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.conf;

import java.util.Properties;

/**
 * A builder that can be used to construct a twitter4j configuration with desired settings.  This
 * builder has sensible defaults such that {@code new ConfigurationBuilder().build()} would create a
 * usable configuration.  This configuration builder is useful for clients that wish to configure
 * twitter4j in unit tests or from command line flags for example.
 *
 * @author John Sirois - john.sirois at gmail.com
 */
public final class ConfigurationBuilder {

    private ConfigurationBase configurationBean = new PropertyConfiguration();

    public ConfigurationBuilder setPrettyDebugEnabled(final boolean prettyDebugEnabled) {
        checkNotBuilt();
        configurationBean.setPrettyDebugEnabled(prettyDebugEnabled);
        return this;
    }

    public ConfigurationBuilder setGZIPEnabled(final boolean gzipEnabled) {
        checkNotBuilt();
        configurationBean.setGZIPEnabled(gzipEnabled);
        return this;
    }

    public ConfigurationBuilder setDebugEnabled(final boolean debugEnabled) {
        checkNotBuilt();
        configurationBean.setDebug(debugEnabled);
        return this;
    }

    public ConfigurationBuilder setApplicationOnlyAuthEnabled(final boolean applicationOnlyAuthEnabled) {
        checkNotBuilt();
        configurationBean.setApplicationOnlyAuthEnabled(applicationOnlyAuthEnabled);
        return this;
    }

    public ConfigurationBuilder setUser(final String user) {
        checkNotBuilt();
        configurationBean.setUser(user);
        return this;
    }

    public ConfigurationBuilder setPassword(final String password) {
        checkNotBuilt();
        configurationBean.setPassword(password);
        return this;
    }

    public ConfigurationBuilder setHttpProxyHost(final String httpProxyHost) {
        checkNotBuilt();
        configurationBean.setHttpProxyHost(httpProxyHost);
        return this;
    }

    public ConfigurationBuilder setHttpProxyUser(final String httpProxyUser) {
        checkNotBuilt();
        configurationBean.setHttpProxyUser(httpProxyUser);
        return this;
    }

    public ConfigurationBuilder setHttpProxyPassword(final String httpProxyPassword) {
        checkNotBuilt();
        configurationBean.setHttpProxyPassword(httpProxyPassword);
        return this;
    }

    public ConfigurationBuilder setHttpProxyPort(final int httpProxyPort) {
        checkNotBuilt();
        configurationBean.setHttpProxyPort(httpProxyPort);
        return this;
    }

    public ConfigurationBuilder setHttpProxySocks(final boolean httpProxySocks) {
        checkNotBuilt();
        configurationBean.setHttpProxySocks(httpProxySocks);
        return this;
    }
    
    public ConfigurationBuilder setHttpConnectionTimeout(final int httpConnectionTimeout) {
        checkNotBuilt();
        configurationBean.setHttpConnectionTimeout(httpConnectionTimeout);
        return this;
    }

    public ConfigurationBuilder setHttpReadTimeout(final int httpReadTimeout) {
        checkNotBuilt();
        configurationBean.setHttpReadTimeout(httpReadTimeout);
        return this;
    }

    public ConfigurationBuilder setHttpStreamingReadTimeout(final int httpStreamingReadTimeout) {
        checkNotBuilt();
        configurationBean.setHttpStreamingReadTimeout(httpStreamingReadTimeout);
        return this;
    }

    public ConfigurationBuilder setHttpRetryCount(final int httpRetryCount) {
        checkNotBuilt();
        configurationBean.setHttpRetryCount(httpRetryCount);
        return this;
    }

    public ConfigurationBuilder setHttpRetryIntervalSeconds(final int httpRetryIntervalSeconds) {
        checkNotBuilt();
        configurationBean.setHttpRetryIntervalSeconds(httpRetryIntervalSeconds);
        return this;
    }

    public ConfigurationBuilder setOAuthConsumerKey(final String oAuthConsumerKey) {
        checkNotBuilt();
        configurationBean.setOAuthConsumerKey(oAuthConsumerKey);
        return this;
    }

    public ConfigurationBuilder setOAuthConsumerSecret(final String oAuthConsumerSecret) {
        checkNotBuilt();
        configurationBean.setOAuthConsumerSecret(oAuthConsumerSecret);
        return this;
    }

    public ConfigurationBuilder setOAuthAccessToken(final String oAuthAccessToken) {
        checkNotBuilt();
        configurationBean.setOAuthAccessToken(oAuthAccessToken);
        return this;
    }

    public ConfigurationBuilder setOAuthAccessTokenSecret(final String oAuthAccessTokenSecret) {
        checkNotBuilt();
        configurationBean.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
        return this;
    }

    public ConfigurationBuilder setOAuth2TokenType(final String oAuth2TokenType) {
        checkNotBuilt();
        configurationBean.setOAuth2TokenType(oAuth2TokenType);
        return this;
    }

    public ConfigurationBuilder setOAuth2AccessToken(final String oAuth2AccessToken) {
        checkNotBuilt();
        configurationBean.setOAuth2AccessToken(oAuth2AccessToken);
        return this;
    }

    public ConfigurationBuilder setOAuth2Scope(final String oAuth2Scope) {
        checkNotBuilt();
        configurationBean.setOAuth2Scope(oAuth2Scope);
        return this;
    }

    public ConfigurationBuilder setOAuthRequestTokenURL(final String oAuthRequestTokenURL) {
        checkNotBuilt();
        configurationBean.setOAuthRequestTokenURL(oAuthRequestTokenURL);
        return this;
    }

    public ConfigurationBuilder setOAuthAuthorizationURL(final String oAuthAuthorizationURL) {
        checkNotBuilt();
        configurationBean.setOAuthAuthorizationURL(oAuthAuthorizationURL);
        return this;
    }

    public ConfigurationBuilder setOAuthAccessTokenURL(final String oAuthAccessTokenURL) {
        checkNotBuilt();
        configurationBean.setOAuthAccessTokenURL(oAuthAccessTokenURL);
        return this;
    }

    public ConfigurationBuilder setOAuthAuthenticationURL(final String oAuthAuthenticationURL) {
        checkNotBuilt();
        configurationBean.setOAuthAuthenticationURL(oAuthAuthenticationURL);
        return this;
    }

    public ConfigurationBuilder setOAuth2TokenURL(final String oAuth2TokenURL) {
        checkNotBuilt();
        configurationBean.setOAuth2TokenURL(oAuth2TokenURL);
        return this;
    }

    public ConfigurationBuilder setOAuth2InvalidateTokenURL(final String invalidateTokenURL) {
        checkNotBuilt();
        configurationBean.setOAuth2InvalidateTokenURL(invalidateTokenURL);
        return this;
    }

    public ConfigurationBuilder setRestBaseURL(final String restBaseURL) {
        checkNotBuilt();
        configurationBean.setRestBaseURL(restBaseURL);
        return this;
    }

    public ConfigurationBuilder setUploadBaseURL(final String uploadBaseURL) {
        checkNotBuilt();
        configurationBean.setUploadBaseURL(uploadBaseURL);
        return this;
    }

    public ConfigurationBuilder setStreamBaseURL(final String streamBaseURL) {
        checkNotBuilt();
        configurationBean.setStreamBaseURL(streamBaseURL);
        return this;
    }

    public ConfigurationBuilder setUserStreamBaseURL(final String userStreamBaseURL) {
        checkNotBuilt();
        configurationBean.setUserStreamBaseURL(userStreamBaseURL);
        return this;
    }

    public ConfigurationBuilder setSiteStreamBaseURL(final String siteStreamBaseURL) {
        checkNotBuilt();
        configurationBean.setSiteStreamBaseURL(siteStreamBaseURL);
        return this;
    }

    public ConfigurationBuilder setAsyncNumThreads(final int asyncNumThreads) {
        checkNotBuilt();
        configurationBean.setAsyncNumThreads(asyncNumThreads);
        return this;
    }

    public ConfigurationBuilder setDaemonEnabled(final boolean daemonEnabled) {
        checkNotBuilt();
        configurationBean.setDaemonEnabled(daemonEnabled);
        return this;
    }

    public ConfigurationBuilder setContributingTo(final long contributingTo) {
        checkNotBuilt();
        configurationBean.setContributingTo(contributingTo);
        return this;
    }

    public ConfigurationBuilder setDispatcherImpl(final String dispatcherImpl) {
        checkNotBuilt();
        configurationBean.setDispatcherImpl(dispatcherImpl);
        return this;
    }

    public ConfigurationBuilder setTrimUserEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setTrimUserEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setIncludeExtAltTextEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setIncludeExtAltTextEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setTweetModeExtended(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setTweetModeExtended(enabled);
        return this;
    }

    public ConfigurationBuilder setIncludeMyRetweetEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setIncludeMyRetweetEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setIncludeEntitiesEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setIncludeEntitiesEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setIncludeEmailEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setIncludeEmailEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setJSONStoreEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setJSONStoreEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setMBeanEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setMBeanEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setUserStreamRepliesAllEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setUserStreamRepliesAllEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setUserStreamWithFollowingsEnabled(final boolean enabled) {
        checkNotBuilt();
        configurationBean.setUserStreamWithFollowingsEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder setMediaProvider(final String mediaProvider) {
        checkNotBuilt();
        configurationBean.setMediaProvider(mediaProvider);
        return this;
    }

    public ConfigurationBuilder setMediaProviderAPIKey(final String mediaProviderAPIKey) {
        checkNotBuilt();
        configurationBean.setMediaProviderAPIKey(mediaProviderAPIKey);
        return this;
    }

    public ConfigurationBuilder setMediaProviderParameters(final Properties props) {
        checkNotBuilt();
        configurationBean.setMediaProviderParameters(props);
        return this;
    }

    public Configuration build() {
        checkNotBuilt();
        configurationBean.cacheInstance();
        try {
            return configurationBean;
        } finally {
            configurationBean = null;
        }
    }

    private void checkNotBuilt() {
        if (configurationBean == null) {
            throw new IllegalStateException("Cannot use this builder any longer, build() has already been called");
        }
    }
}
