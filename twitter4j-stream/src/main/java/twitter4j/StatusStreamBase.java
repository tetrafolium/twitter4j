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

package twitter4j;

import twitter4j.conf.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
abstract class StatusStreamBase implements StatusStream {
    static final Logger logger = Logger.getLogger(StatusStreamImpl.class);

    private boolean streamAlive = true;
    private BufferedReader br;
    private InputStream is;
    private HttpResponse response;
    private final Dispatcher dispatcher;
    final Configuration CONF;
    private ObjectFactory factory;

    /*package*/

    StatusStreamBase(final Dispatcher dispatcher, final InputStream stream, final Configuration conf) throws IOException {
        this.is = stream;
        this.br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        this.dispatcher = dispatcher;
        this.CONF = conf;
        this.factory = new JSONImplFactory(conf);
    }
    /*package*/

    StatusStreamBase(final Dispatcher dispatcher, final HttpResponse response, final Configuration conf) throws IOException {
        this(dispatcher, response.asStream(), conf);
        this.response = response;
    }

    String parseLine(final String line) {
        return line;
    }

    abstract class StreamEvent implements Runnable {
        String line;

        StreamEvent(final String line) {
            this.line = line;
        }
    }

    void handleNextElement(final StreamListener[] listeners,
                           final RawStreamListener[] rawStreamListeners) throws TwitterException {
        if (!streamAlive) {
            throw new IllegalStateException("Stream already closed.");
        }
        try {
            String line = br.readLine();
            if (null == line) {
                //invalidate this status stream
                throw new IOException("the end of the stream has been reached");
            }
            dispatcher.invokeLater(new StreamEvent(line) {
                @Override
                public void run() {
                    try {
                        if (rawStreamListeners.length > 0) {
                            onMessage(line, rawStreamListeners);
                        }
                        // SiteStreamsImpl will parse "forUser" attribute
                        line = parseLine(line);
                        if (line != null && line.length() > 0) {
                            // parsing JSON is an expensive process and can be avoided when all listeners are instanceof RawStreamListener
                            if (listeners.length > 0) {
                                if (CONF.isJSONStoreEnabled()) {
                                    TwitterObjectFactory.clearThreadLocalMap();
                                }
                                JSONObject json = new JSONObject(line);
                                JSONObjectType.Type event = JSONObjectType.determine(json);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Received:", CONF.getHttpClientConfiguration().isPrettyDebugEnabled() ? json.toString(1) : json.toString());
                                }
                                switch (event) {
                                    case SENDER:
                                        onSender(json, listeners);
                                        break;
                                    case STATUS:
                                        onStatus(json, listeners);
                                        break;
                                    case DIRECT_MESSAGE:
                                        onDirectMessage(json, listeners);
                                        break;
                                    case DELETE:
                                        onDelete(json, listeners);
                                        break;
                                    case LIMIT:
                                        onLimit(json, listeners);
                                        break;
                                    case STALL_WARNING:
                                        onStallWarning(json, listeners);
                                        break;
                                    case SCRUB_GEO:
                                        onScrubGeo(json, listeners);
                                        break;
                                    case FRIENDS:
                                        onFriends(json, listeners);
                                        break;
                                    case FAVORITE:
                                        onFavorite(json.getJSONObject("source"), json.getJSONObject("target"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case UNFAVORITE:
                                        onUnfavorite(json.getJSONObject("source"), json.getJSONObject("target"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case FOLLOW:
                                        onFollow(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
                                        break;
                                    case UNFOLLOW:
                                        onUnfollow(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
                                        break;
                                    case USER_LIST_MEMBER_ADDED:
                                        onUserListMemberAddition(json.getJSONObject("target"), json.getJSONObject("source"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case USER_LIST_MEMBER_DELETED:
                                        onUserListMemberDeletion(json.getJSONObject("target"), json.getJSONObject("source"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case USER_LIST_SUBSCRIBED:
                                        onUserListSubscription(json.getJSONObject("source"), json.getJSONObject("target"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case USER_LIST_UNSUBSCRIBED:
                                        onUserListUnsubscription(json.getJSONObject("source"), json.getJSONObject("target"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case USER_LIST_CREATED:
                                        onUserListCreation(json.getJSONObject("source"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case USER_LIST_UPDATED:
                                        onUserListUpdated(json.getJSONObject("source"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case USER_LIST_DESTROYED:
                                        onUserListDestroyed(json.getJSONObject("source"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case USER_UPDATE:
                                        onUserUpdate(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
                                        break;
                                    case USER_DELETE:
                                        onUserDeletion(json.getLong("target"), listeners);
                                        break;
                                    case USER_SUSPEND:
                                        onUserSuspension(json.getLong("target"), listeners);
                                        break;
                                    case BLOCK:
                                        onBlock(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
                                        break;
                                    case UNBLOCK:
                                        onUnblock(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
                                        break;
                                    case RETWEETED_RETWEET:
                                        onRetweetedRetweet(json.getJSONObject("source"), json.getJSONObject("target"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case FAVORITED_RETWEET:
                                        onFavoritedRetweet(json.getJSONObject("source"), json.getJSONObject("target"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case QUOTED_TWEET:
                                        onQuotedTweet(json.getJSONObject("source"), json.getJSONObject("target"), json.getJSONObject("target_object"), listeners);
                                        break;
                                    case DISCONNECTION:
                                        onDisconnectionNotice(line, listeners);
                                        break;
                                    case MUTE:
                                        onMute(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
                                        break;
                                    case UNMUTE:
                                        onUnmute(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
                                        break;
                                    case UNKNOWN:
                                    default:
                                        logger.warn("Received unknown event:", CONF.getHttpClientConfiguration().isPrettyDebugEnabled() ? json.toString(1) : json.toString());
                                }
                            }
                        }
                    } catch (Exception ex) {
                        onException(ex, listeners);
                    }
                }
            });

        } catch (IOException ioe) {
            try {
                is.close();
            } catch (IOException ignore) {
            }
            boolean isUnexpectedException = streamAlive;
            streamAlive = false;
            onClose();
            if (isUnexpectedException) {
                throw new TwitterException("Stream closed.", ioe);
            }
        }
    }

    void onMessage(final String rawString, final RawStreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onMessage");
    }

    void onSender(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onSender");
    }

    void onStatus(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onStatus");
    }

    void onDirectMessage(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onDirectMessage");
    }

    void onDelete(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onDelete");
    }

    void onLimit(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onLimit");
    }

    void onStallWarning(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onStallWarning");
    }

    void onScrubGeo(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onScrubGeo");
    }

    void onFriends(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onFriends");
    }

    void onFavorite(final JSONObject source, final JSONObject target, final JSONObject targetObject, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onFavorite");
    }

    void onUnfavorite(final JSONObject source, final JSONObject target, final JSONObject targetObject, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onUnfavorite");
    }

    void onFollow(final JSONObject source, final JSONObject target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onFollow");
    }

    void onUnfollow(final JSONObject source, final JSONObject target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onUnfollow");
    }

    void onUserListMemberAddition(final JSONObject addedMember, final JSONObject owner, final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onUserListMemberAddition");
    }

    void onUserListMemberDeletion(final JSONObject deletedMember, final JSONObject owner, final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onUserListMemberDeletion");
    }

    void onUserListSubscription(final JSONObject source, final JSONObject owner, final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onUserListSubscription");
    }

    void onUserListUnsubscription(final JSONObject source, final JSONObject owner, final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onUserListUnsubscription");
    }

    void onUserListCreation(final JSONObject source, final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onUserListCreation");
    }

    void onUserListUpdated(final JSONObject source, final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
        logger.warn("Unhandled event: onUserListUpdated");
    }

    void onUserListDestroyed(final JSONObject source, final JSONObject userList, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onUserListDestroyed");
    }

    void onUserUpdate(final JSONObject source, final JSONObject target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onUserUpdate");
    }

    void onUserDeletion(final long target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onUserDeletion");
    }

    void onUserSuspension(final long target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onUserSuspension");
    }

    void onBlock(final JSONObject source, final JSONObject target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onBlock");
    }

    void onUnblock(final JSONObject source, final JSONObject target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onUnblock");
    }

    void onRetweetedRetweet(final JSONObject source, final JSONObject target, final JSONObject targetObject, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onRetweetedRetweet");
    }
    void onFavoritedRetweet(final JSONObject source, final JSONObject target, final JSONObject targetObject, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onFavoritedRetweet");
    }

    void onQuotedTweet(final JSONObject source, final JSONObject target, final JSONObject targetObject, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onQuotedTweet");
    }

    void onMute(final JSONObject source, final JSONObject target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onMute");
    }

    void onUnmute(final JSONObject source, final JSONObject target, final StreamListener[] listeners) throws TwitterException {
        logger.warn("Unhandled event: onUnmute");
    }

    void onDisconnectionNotice(final String line, final StreamListener[] listeners) {
        logger.warn("Unhandled event: ", line);
    }

    void onException(final Exception e, final StreamListener[] listeners) {
        logger.warn("Unhandled event: ", e.getMessage());
    }

    protected abstract void onClose();

    @Override
    public void close() throws IOException {
        streamAlive = false;
        is.close();
        br.close();
        if (response != null) {
            response.disconnect();
        }
        onClose();
    }

    Status asStatus(final JSONObject json) throws TwitterException {
        Status status = new StatusJSONImpl(json);

        if (CONF.isJSONStoreEnabled()) {
            TwitterObjectFactory.registerJSONObject(status, json);
        }
        return status;
    }

    DirectMessage asDirectMessage(final JSONObject json) throws TwitterException {
        try {
            JSONObject dmJSON = json.getJSONObject("direct_message");
            DirectMessage directMessage = new DirectMessageJSONImpl(dmJSON);
            if (CONF.isJSONStoreEnabled()) {
                TwitterObjectFactory.registerJSONObject(directMessage, dmJSON);
            }
            return directMessage;
        } catch (JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }

    long[] asFriendList(final JSONObject json) throws TwitterException {
        JSONArray friends;
        try {
            friends = json.getJSONArray("friends");
            long[] friendIds = new long[friends.length()];
            for (int i = 0; i < friendIds.length; ++i) {
                friendIds[i] = Long.parseLong(friends.getString(i));
            }
            return friendIds;
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }

    User asUser(final JSONObject json) throws TwitterException {
        User user = new UserJSONImpl(json);
        if (CONF.isJSONStoreEnabled()) {
            TwitterObjectFactory.registerJSONObject(user, json);
        }
        return user;
    }

    UserList asUserList(final JSONObject json) throws TwitterException {
        UserList userList = new UserListJSONImpl(json);
        if (CONF.isJSONStoreEnabled()) {
            TwitterObjectFactory.registerJSONObject(userList, json);
        }
        return userList;
    }

    @Override
    public abstract void next(StatusListener listener) throws TwitterException;

    public abstract void next(StreamListener[] listeners, RawStreamListener[] rawStreamListeners) throws TwitterException;

    public void onException(final Exception e, final StreamListener[] listeners, final RawStreamListener[] rawStreamListeners) {
        for (StreamListener listener : listeners) {
            listener.onException(e);
        }
        for (RawStreamListener listener : rawStreamListeners) {
            listener.onException(e);
        }
    }
}
