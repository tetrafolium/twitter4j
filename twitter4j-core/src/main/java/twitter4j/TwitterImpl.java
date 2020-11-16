/*
 * Copyright (C) 2007 Yusuke Yamamoto
 * Copyright (C) 2011 Twitter, Inc.
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

import twitter4j.api.*;
import twitter4j.auth.Authorization;
import twitter4j.conf.Configuration;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static twitter4j.HttpParameter.getParameterArray;

/**
 * A java representation of the <a href="https://dev.twitter.com/docs/api">Twitter REST API</a><br>
 * This class is thread safe and can be cached/re-used and used concurrently.<br>
 * Currently this class is not carefully designed to be extended. It is suggested to extend this class only for mock testing purpose.<br>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
class TwitterImpl extends TwitterBaseImpl implements Twitter {
    private static final long serialVersionUID = 9170943084096085770L;
    private static final Logger logger = Logger.getLogger(TwitterBaseImpl.class);
    
    private final String IMPLICIT_PARAMS_STR;
    private final HttpParameter[] IMPLICIT_PARAMS;
    private final HttpParameter INCLUDE_MY_RETWEET;
    
    private final String CHUNKED_INIT = "INIT";
    private final String CHUNKED_APPEND = "APPEND";
    private final String CHUNKED_FINALIZE = "FINALIZE";
    private final String CHUNKED_STATUS = "STATUS";
    
        private final int MB = 1024 * 1024; // 1 MByte
        private final int MAX_VIDEO_SIZE = 512 * MB; // 512MB is a constraint  imposed by Twitter for video files
        private final int CHUNK_SIZE = 2 * MB; // max chunk size

    private static final ConcurrentHashMap<Configuration, HttpParameter[]> implicitParamsMap = new ConcurrentHashMap<Configuration, HttpParameter[]>();
    private static final ConcurrentHashMap<Configuration, String> implicitParamsStrMap = new ConcurrentHashMap<Configuration, String>();

    /*package*/
    TwitterImpl(final Configuration conf, final Authorization auth) {
        super(conf, auth);
        INCLUDE_MY_RETWEET = new HttpParameter("include_my_retweet", conf.isIncludeMyRetweetEnabled());
        if (implicitParamsMap.containsKey(conf)) {
            this.IMPLICIT_PARAMS = implicitParamsMap.get(conf);
            this.IMPLICIT_PARAMS_STR = implicitParamsStrMap.get(conf);
        } else {
            String implicitParamsStr = conf.isIncludeEntitiesEnabled() ? "include_entities=" + true : "";
            boolean contributorsEnabled = conf.getContributingTo() != -1L;
            if (contributorsEnabled) {
                if (!"".equals(implicitParamsStr)) {
                    implicitParamsStr += "&";
                }
                implicitParamsStr += "contributingto=" + conf.getContributingTo();
            }

            if (conf.isTweetModeExtended()) {
                if (!"".equals(implicitParamsStr)) {
                    implicitParamsStr += "&";
                }
                implicitParamsStr += "tweet_mode=extended";
            }

            List<HttpParameter> params = new ArrayList<HttpParameter>(3);
            if (conf.isIncludeEntitiesEnabled()) {
                params.add(new HttpParameter("include_entities", "true"));
            }
            if (contributorsEnabled) {
                params.add(new HttpParameter("contributingto", conf.getContributingTo()));
            }
            if (conf.isTrimUserEnabled()) {
                params.add(new HttpParameter("trim_user", "1"));
            }
            if (conf.isIncludeExtAltTextEnabled()) {
                params.add(new HttpParameter("include_ext_alt_text", "true"));
            }
            if (conf.isTweetModeExtended()) {
                params.add(new HttpParameter("tweet_mode", "extended"));
            }
            HttpParameter[] implicitParams = params.toArray(new HttpParameter[params.size()]);

            // implicitParamsMap.containsKey() is evaluated in the above if clause.
            // thus implicitParamsStrMap needs to be initialized first
            implicitParamsStrMap.putIfAbsent(conf, implicitParamsStr);
            implicitParamsMap.putIfAbsent(conf, implicitParams);

            this.IMPLICIT_PARAMS = implicitParams;
            this.IMPLICIT_PARAMS_STR = implicitParamsStr;
        }
    }

    /* Timelines Resources */

    @Override
    public ResponseList<Status> getMentionsTimeline() throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/mentions_timeline.json"));
    }

    @Override
    public ResponseList<Status> getMentionsTimeline(final Paging paging) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL()
                + "statuses/mentions_timeline.json", paging.asPostParameterArray()));
    }

    @Override
    public ResponseList<Status> getHomeTimeline() throws
            TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL()
                + "statuses/home_timeline.json", INCLUDE_MY_RETWEET));
    }

    @Override
    public ResponseList<Status> getHomeTimeline(final Paging paging) throws
            TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL()
                + "statuses/home_timeline.json", mergeParameters(paging.asPostParameterArray(), new HttpParameter[]{INCLUDE_MY_RETWEET})));
    }

    @Override
    public ResponseList<Status> getRetweetsOfMe() throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL()
                + "statuses/retweets_of_me.json"));
    }

    @Override
    public ResponseList<Status> getRetweetsOfMe(final Paging paging) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL()
                + "statuses/retweets_of_me.json", paging.asPostParameterArray()));
    }

    @Override
    public ResponseList<Status> getUserTimeline(final String screenName, final Paging paging)
            throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL()
                        + "statuses/user_timeline.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("screen_name", screenName)
, INCLUDE_MY_RETWEET}
, paging.asPostParameterArray())
        ));
    }

    @Override
    public ResponseList<Status> getUserTimeline(final long userId, final Paging paging)
            throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL()
                        + "statuses/user_timeline.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("user_id", userId)
, INCLUDE_MY_RETWEET}
, paging.asPostParameterArray())
        ));
    }

    @Override
    public ResponseList<Status> getUserTimeline(final String screenName) throws TwitterException {
        return getUserTimeline(screenName, new Paging());
    }

    @Override
    public ResponseList<Status> getUserTimeline(final long userId) throws TwitterException {
        return getUserTimeline(userId, new Paging());
    }

    @Override
    public ResponseList<Status> getUserTimeline() throws
            TwitterException {
        return getUserTimeline(new Paging());
    }

    @Override
    public ResponseList<Status> getUserTimeline(final Paging paging) throws
            TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL()
                        + "statuses/user_timeline.json",
                mergeParameters(new HttpParameter[]{INCLUDE_MY_RETWEET}
, paging.asPostParameterArray())
        ));
    }

    /* Tweets Resources */

    @Override
    public ResponseList<Status> getRetweets(final long statusId) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweets/" + statusId
                + ".json?count=100"));
    }

    @Override
    public IDs getRetweeterIds(final long statusId, final long cursor) throws TwitterException {
        return getRetweeterIds(statusId, 100, cursor);
    }

    @Override
    public IDs getRetweeterIds(final long statusId, final int count, final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "statuses/retweeters/ids.json?id=" + statusId
                + "&cursor=" + cursor + "&count=" + count));
    }

    @Override
    public Status showStatus(final long id) throws TwitterException {
        return factory.createStatus(get(conf.getRestBaseURL() + "statuses/show/" + id + ".json", new HttpParameter[]{INCLUDE_MY_RETWEET}));
    }

    @Override
    public Status destroyStatus(final long statusId) throws TwitterException {
        return factory.createStatus(post(conf.getRestBaseURL() + "statuses/destroy/" + statusId + ".json"));
    }

    @Override
    public Status updateStatus(final String status) throws TwitterException {
        return factory.createStatus(post(conf.getRestBaseURL() + "statuses/update.json",
                new HttpParameter[]{new HttpParameter("status", status)}));
    }

    @Override
    public Status updateStatus(final StatusUpdate status) throws TwitterException {
        String url = conf.getRestBaseURL() + (status.isForUpdateWithMedia()
                ? "statuses/update_with_media.json" : "statuses/update.json");
        return factory.createStatus(post(url, status.asHttpParameterArray()));
    }

    @Override
    public Status retweetStatus(final long statusId) throws TwitterException {
        return factory.createStatus(post(conf.getRestBaseURL() + "statuses/retweet/" + statusId + ".json"));
    }

    @Override
    public Status unRetweetStatus(final long statusId) throws TwitterException {
        return factory.createStatus(post(conf.getRestBaseURL() + "statuses/unretweet/" + statusId + ".json"));
    }

    @Override
    public OEmbed getOEmbed(final OEmbedRequest req) throws TwitterException {
        return factory.createOEmbed(get(conf.getRestBaseURL()
                + "statuses/oembed.json", req.asHttpParameterArray()));
    }

    @Override
    public ResponseList<Status> lookup(final long... ids) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/lookup.json?id=" + StringUtil.join(ids)));
    }

    @Override
    public UploadedMedia uploadMedia(final File image) throws TwitterException {
        checkFileValidity(image);
        return new UploadedMedia(post(conf.getUploadBaseURL() + "media/upload.json"
, new HttpParameter("media", image)).asJSONObject());
    }

    @Override
    public UploadedMedia uploadMedia(final String fileName, final InputStream image) throws TwitterException {
        return new UploadedMedia(post(conf.getUploadBaseURL() + "media/upload.json"
, new HttpParameter("media", fileName, image)).asJSONObject());
    }

        @Override
        public UploadedMedia uploadMediaChunked(final String fileName, final InputStream media) throws TwitterException {
                //If the InputStream is remote, this is will download it into memory speeding up the chunked upload process 
                byte[] dataBytes = null;
                try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(256 * 1024);
            byte[] buffer = new byte[32768];
            int n;
            while ((n = media.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            dataBytes = baos.toByteArray();
            if (dataBytes.length > MAX_VIDEO_SIZE) {
                                throw new TwitterException(String.format(Locale.US,
                                                "video file can't be longer than: %d MBytes",
						MAX_VIDEO_SIZE / MB));
			}
		} catch (IOException ioe) {
			throw new TwitterException("Failed to download the file.", ioe);
		}
		
		try {

			UploadedMedia uploadedMedia = uploadMediaChunkedInit(dataBytes.length);
			//no need to close ByteArrayInputStream
			ByteArrayInputStream dataInputStream = new ByteArrayInputStream(dataBytes);
			
			byte[] segmentData = new byte[CHUNK_SIZE];
			int segmentIndex = 0;
			int totalRead = 0;
			int bytesRead = 0;
			
			while ((bytesRead = dataInputStream.read(segmentData)) > 0) {
				totalRead = totalRead + bytesRead;
				logger.debug("Chunked appened, segment index:" + segmentIndex + " bytes:" + totalRead + "/" + dataBytes.length);
				//no need to close ByteArrayInputStream
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(segmentData, 0, bytesRead);
				uploadMediaChunkedAppend(fileName, byteArrayInputStream, segmentIndex, uploadedMedia.getMediaId());
				segmentData = new byte[CHUNK_SIZE];
				segmentIndex++;
			}
			return uploadMediaChunkedFinalize(uploadedMedia.getMediaId());
		} catch (Exception e) {
			 throw new TwitterException(e);
		}
	}
    
	// twurl -H upload.twitter.com "/1.1/media/upload.json" -d
	// "command=INIT&media_type=video/mp4&total_bytes=4430752"
    
    private UploadedMedia uploadMediaChunkedInit(final long size) throws TwitterException {
		return new UploadedMedia(post(
				conf.getUploadBaseURL() + "media/upload.json",
				new HttpParameter[] {new HttpParameter("command", CHUNKED_INIT),
						new HttpParameter("media_type", "video/mp4"), 
						new HttpParameter("media_category", "tweet_video"),
						new HttpParameter("total_bytes", size) })
				.asJSONObject());
	}

	// twurl -H upload.twitter.com "/1.1/media/upload.json" -d
	// "command=APPEND&media_id=601413451156586496&segment_index=0" --file
	// /path/to/video.mp4 --file-field "media"

    private void uploadMediaChunkedAppend(final String fileName, final InputStream media, final int segmentIndex, final long mediaId) throws TwitterException {
		post(conf.getUploadBaseURL() + "media/upload.json", new HttpParameter[] {
				new HttpParameter("command", CHUNKED_APPEND), new HttpParameter("media_id", mediaId),
				new HttpParameter("segment_index", segmentIndex), new HttpParameter("media", fileName, media) });
	}

	// twurl -H upload.twitter.com "/1.1/media/upload.json" -d
	// "command=FINALIZE&media_id=601413451156586496"

	private UploadedMedia uploadMediaChunkedFinalize(final long mediaId) throws TwitterException {
		int tries = 0;
		int maxTries = 20;
		int lastProgressPercent = 0;
		int currentProgressPercent = 0;
		UploadedMedia uploadedMedia = uploadMediaChunkedFinalize0(mediaId);
		while (tries < maxTries) {
			if (lastProgressPercent == currentProgressPercent) {
				tries++;
			}
			lastProgressPercent = currentProgressPercent;
			String state = uploadedMedia.getProcessingState();
			if (state.equals("failed")) {
				throw new TwitterException("Failed to finalize the chuncked upload.");
			}
			if (state.equals("pending") || state.equals("in_progress")) {
				currentProgressPercent = uploadedMedia.getProgressPercent();
				int waitSec = Math.max(uploadedMedia.getProcessingCheckAfterSecs(), 1);
				logger.debug("Chunked finalize, wait for:" + waitSec + " sec");
				try {
					Thread.sleep(waitSec * 1000);
				} catch (InterruptedException e) {
					throw new TwitterException("Failed to finalize the chuncked upload.", e);
				}
			}
			if (state.equals("succeeded")) {
				return uploadedMedia;
			}
			uploadedMedia = uploadMediaChunkedStatus(mediaId);
		} 
		throw new TwitterException("Failed to finalize the chuncked upload, progress has stopped, tried " + tries + 1 + " times.");
	}
	
	private UploadedMedia uploadMediaChunkedFinalize0(final long mediaId) throws TwitterException {
		JSONObject json = post(
				conf.getUploadBaseURL() + "media/upload.json",
				new HttpParameter[] {
						new HttpParameter("command", CHUNKED_FINALIZE),
						new HttpParameter("media_id", mediaId) })
				.asJSONObject();
		logger.debug("Finalize response:" + json);
		return new UploadedMedia(json);
	}
	
	private UploadedMedia uploadMediaChunkedStatus(final long mediaId) throws TwitterException {
		JSONObject json = get(
				conf.getUploadBaseURL() + "media/upload.json",
				new HttpParameter[] {
						new HttpParameter("command", CHUNKED_STATUS),
						new HttpParameter("media_id", mediaId) })
				.asJSONObject();
		logger.debug("Status response:" + json);
		return new UploadedMedia(json);
	}
    
    /* Search Resources */

    @Override
    public QueryResult search(final Query query) throws TwitterException {
        if (query.nextPage() != null) {
            return factory.createQueryResult(get(conf.getRestBaseURL()
                    + "search/tweets.json" + query.nextPage()), query);
        } else {
            return factory.createQueryResult(get(conf.getRestBaseURL()
                    + "search/tweets.json", query.asHttpParameterArray()), query);
        }
    }

    /* Direct Messages Resources */


    @Override
    public DirectMessageList getDirectMessages(final int count) throws TwitterException {
        return factory.createDirectMessageList(get(conf.getRestBaseURL() + "direct_messages/events/list.json"
, new HttpParameter("count", count)));
    }

    @Override
    public DirectMessageList getDirectMessages(final int count, final String cursor) throws TwitterException {
        return factory.createDirectMessageList(get(conf.getRestBaseURL() + "direct_messages/events/list.json"
, new HttpParameter("count", count)
, new HttpParameter("cursor", cursor)));
    }


    @Override
    public DirectMessage showDirectMessage(final long id) throws TwitterException {
        return factory.createDirectMessage(get(conf.getRestBaseURL() + "direct_messages/events/show.json?id=" + id));
    }

    @Override
    public void destroyDirectMessage(final long id) throws TwitterException {
        ensureAuthorizationEnabled();
        http.delete(conf.getRestBaseURL() + "direct_messages/events/destroy.json?id=" + id, null, auth, null);
    }

    @Override
    public DirectMessage sendDirectMessage(final long recipientId, final String text, final QuickReply... quickReplies)
            throws TwitterException {
        try {
            return factory.createDirectMessage(post(conf.getRestBaseURL() + "direct_messages/events/new.json",
                    createMessageCreateJsonObject(recipientId, text, -1L,  null, quickReplies)));
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }
    @Override
    public DirectMessage sendDirectMessage(final long recipientId, final String text, final String quickReplyResponse)
            throws TwitterException {
        try {
            return factory.createDirectMessage(post(conf.getRestBaseURL() + "direct_messages/events/new.json",
                    createMessageCreateJsonObject(recipientId, text, -1L,  quickReplyResponse)));
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }

    private static JSONObject createMessageCreateJsonObject(final long recipientId, final String text, final long mediaId, final String quickReplyResponse, final QuickReply... quickReplies) throws JSONException {
        String type = mediaId == -1 ? null : "media";

        final JSONObject messageDataJSON = new JSONObject();

        final JSONObject target = new JSONObject();
        target.put("recipient_id", recipientId);
        messageDataJSON.put("target", target);

        final JSONObject messageData = new JSONObject();
        messageData.put("text", text);
        if (type != null && mediaId != -1) {
            final JSONObject attachment = new JSONObject();
            attachment.put("type", type);
            if (type.equals("media")) {
                final JSONObject media = new JSONObject();
                media.put("id", mediaId);
                attachment.put("media", media);
            }
            messageData.put("attachment", attachment);
        }
        // https://developer.twitter.com/en/docs/direct-messages/quick-replies/api-reference/options
        if (quickReplies.length > 0) {
            JSONObject quickReplyJSON = new JSONObject();
            quickReplyJSON.put("type", "options");
            JSONArray jsonArray = new JSONArray();
            for (QuickReply quickReply : quickReplies) {
                JSONObject option = new JSONObject();
                option.put("label", quickReply.getLabel());
                if (quickReply.getDescription() != null) {
                    option.put("description", quickReply.getDescription());
                }
                if (quickReply.getMetadata() != null) {
                    option.put("metadata", quickReply.getMetadata());
                }
                jsonArray.put(option);
            }
            quickReplyJSON.put("options", jsonArray);
            messageData.put("quick_reply", quickReplyJSON);
        }
        if (quickReplyResponse != null) {
            JSONObject quickReplyResponseJSON = new JSONObject();
            quickReplyResponseJSON.put("type", "options");
            quickReplyResponseJSON.put("metadata", quickReplyResponse);
            messageData.put("quick_reply_response", quickReplyResponseJSON);
        }
        messageDataJSON.put("message_data", messageData);

        final JSONObject json = new JSONObject();
        final JSONObject event = new JSONObject();
        event.put("type", "message_create");
        event.put("message_create", messageDataJSON);
        json.put("event", event);

        return json;
    }

    @Override
    public DirectMessage sendDirectMessage(final long recipientId, final String text, final long mediaId)
            throws TwitterException {
        try {
            return factory.createDirectMessage(post(conf.getRestBaseURL() + "direct_messages/events/new.json",
                    createMessageCreateJsonObject(recipientId, text, mediaId, null)));
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }

    @Override
    public DirectMessage sendDirectMessage(final long recipientId, final String text)
        throws TwitterException {
        return this.sendDirectMessage(recipientId, text, -1L);
    }

    @Override
    public DirectMessage sendDirectMessage(final String screenName, final String text) throws TwitterException {
        return this.sendDirectMessage(showUser(screenName).getId(), text);
    }

    @Override
    public InputStream getDMImageAsStream(final String url) throws TwitterException {
        return get(url).asStream();
    }

    /* Friends & Followers Resources */

    @Override
    public IDs getNoRetweetsFriendships() throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "friendships/no_retweets/ids.json"));
    }

    @Override
    public IDs getFriendsIDs(final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "friends/ids.json?cursor=" + cursor));
    }

    @Override
    public IDs getFriendsIDs(final long userId, final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "friends/ids.json?user_id=" + userId
                + "&cursor=" + cursor));
    }

    @Override
    public IDs getFriendsIDs(final long userId, final long cursor, final int count) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "friends/ids.json?user_id=" + userId
                + "&cursor=" + cursor + "&count=" + count));
    }

    @Override
    public IDs getFriendsIDs(final String screenName, final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "friends/ids.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("cursor", cursor)));
    }

    @Override
    public IDs getFriendsIDs(final String screenName, final long cursor, final int count) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "friends/ids.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("cursor", cursor),
                new HttpParameter("count", count)));
    }

    @Override
    public IDs getFollowersIDs(final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "followers/ids.json?cursor=" + cursor));
    }

    @Override
    public IDs getFollowersIDs(final long userId, final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "followers/ids.json?user_id=" + userId
                + "&cursor=" + cursor));
    }

    @Override
    public IDs getFollowersIDs(final long userId, final long cursor, final int count) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "followers/ids.json?user_id=" + userId
                + "&cursor=" + cursor + "&count=" + count));
    }

    @Override
    public IDs getFollowersIDs(final String screenName, final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "followers/ids.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("cursor", cursor)));
    }

    @Override
    public IDs getFollowersIDs(final String screenName, final long cursor, final int count) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "followers/ids.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("cursor", cursor),
                new HttpParameter("count", count)));
    }

    @Override
    public ResponseList<Friendship> lookupFriendships(final long... ids) throws TwitterException {
        return factory.createFriendshipList(get(conf.getRestBaseURL() + "friendships/lookup.json?user_id=" + StringUtil.join(ids)));
    }

    @Override
    public ResponseList<Friendship> lookupFriendships(final String... screenNames) throws TwitterException {
        return factory.createFriendshipList(get(conf.getRestBaseURL()
                + "friendships/lookup.json?screen_name=" + StringUtil.join(screenNames)));
    }

    @Override
    public IDs getIncomingFriendships(final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "friendships/incoming.json?cursor=" + cursor));
    }

    @Override
    public IDs getOutgoingFriendships(final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "friendships/outgoing.json?cursor=" + cursor));
    }

    @Override
    public User createFriendship(final long userId) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "friendships/create.json?user_id=" + userId));
    }

    @Override
    public User createFriendship(final String screenName) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "friendships/create.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName)
                }
        ));
    }

    @Override
    public User createFriendship(final long userId, final boolean follow) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "friendships/create.json?user_id=" + userId + "&follow="
                + follow));
    }

    @Override
    public User createFriendship(final String screenName, final boolean follow) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "friendships/create.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName),
                        new HttpParameter("follow", follow)
                }
        ));
    }

    @Override
    public User destroyFriendship(final long userId) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "friendships/destroy.json?user_id=" + userId));
    }

    @Override
    public User destroyFriendship(final String screenName) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "friendships/destroy.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName)
                }
        ));
    }

    @Override
    public Relationship updateFriendship(final long userId, final boolean enableDeviceNotification
, final boolean retweets) throws TwitterException {
        return factory.createRelationship((post(conf.getRestBaseURL() + "friendships/update.json",
                new HttpParameter("user_id", userId),
                new HttpParameter("device", enableDeviceNotification),
                new HttpParameter("retweets", retweets))));
    }

    @Override
    public Relationship updateFriendship(final String screenName, final boolean enableDeviceNotification
, final boolean retweets) throws TwitterException {
        return factory.createRelationship(post(conf.getRestBaseURL() + "friendships/update.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("device", enableDeviceNotification),
                new HttpParameter("retweets", retweets)));
    }

    @Override
    public Relationship showFriendship(final long sourceId, final long targetId) throws TwitterException {
        return factory.createRelationship(get(conf.getRestBaseURL() + "friendships/show.json"
, new HttpParameter("source_id", sourceId), new HttpParameter("target_id", targetId)));
    }

    @Override
    public Relationship showFriendship(final String sourceScreenName, final String targetScreenName) throws TwitterException {
        return factory.createRelationship(get(conf.getRestBaseURL() + "friendships/show.json",
                getParameterArray("source_screen_name", sourceScreenName, "target_screen_name", targetScreenName)));
    }

    @Override
    public PagableResponseList<User> getFriendsList(final long userId, final long cursor) throws TwitterException {
        return getFriendsList(userId, cursor, 20);
    }

    @Override
    public PagableResponseList<User> getFriendsList(final long userId, final long cursor, final int count) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "friends/list.json?user_id=" + userId
                + "&cursor=" + cursor + "&count=" + count));
    }

    @Override
    public PagableResponseList<User> getFriendsList(final String screenName, final long cursor) throws TwitterException {
        return getFriendsList(screenName, cursor, 20);
    }

    @Override
    public PagableResponseList<User> getFriendsList(final String screenName, final long cursor, final int count) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "friends/list.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("cursor", cursor),
                new HttpParameter("count", count)));
    }

    @Override
    public PagableResponseList<User> getFriendsList(final long userId, final long cursor, final int count, final boolean skipStatus, final boolean includeUserEntities) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "friends/list.json?user_id=" + userId
                + "&cursor=" + cursor + "&count=" + count
                + "&skip_status=" + skipStatus + "&include_user_entities=" + includeUserEntities));
    }

    @Override
    public PagableResponseList<User> getFriendsList(final String screenName, final long cursor, final int count,
                                                    final boolean skipStatus, final boolean includeUserEntities) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "friends/list.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("cursor", cursor),
                new HttpParameter("count", count),
                new HttpParameter("skip_status", skipStatus),
                new HttpParameter("include_user_entities", includeUserEntities)));
    }

    @Override
    public PagableResponseList<User> getFollowersList(final long userId, final long cursor) throws TwitterException {
        return getFollowersList(userId, cursor, 20);
    }

    @Override
    public PagableResponseList<User> getFollowersList(final String screenName, final long cursor) throws TwitterException {
        return getFollowersList(screenName, cursor, 20);
    }

    @Override
    public PagableResponseList<User> getFollowersList(final long userId, final long cursor, final int count) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "followers/list.json?user_id=" + userId
                + "&cursor=" + cursor + "&count=" + count));
    }

    @Override
    public PagableResponseList<User> getFollowersList(final String screenName, final long cursor, final int count) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "followers/list.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("cursor", cursor),
                new HttpParameter("count", count)));
    }

    @Override
    public PagableResponseList<User> getFollowersList(final long userId, final long cursor, final int count,
                                                      final boolean skipStatus, final boolean includeUserEntities) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "followers/list.json?user_id=" + userId
                + "&cursor=" + cursor + "&count=" + count
                + "&skip_status=" + skipStatus + "&include_user_entities=" + includeUserEntities));
    }

    @Override
    public PagableResponseList<User> getFollowersList(final String screenName, final long cursor, final int count,
                                                      final boolean skipStatus, final boolean includeUserEntities) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "followers/list.json",
                new HttpParameter("screen_name", screenName),
                new HttpParameter("cursor", cursor),
                new HttpParameter("count", count),
                new HttpParameter("skip_status", skipStatus),
                new HttpParameter("include_user_entities", includeUserEntities)));
    }

    /* Users Resources */

    @Override
    public AccountSettings getAccountSettings() throws TwitterException {
        return factory.createAccountSettings(get(conf.getRestBaseURL() + "account/settings.json"));
    }

    @Override
    public User verifyCredentials() throws TwitterException {
        return super.fillInIDAndScreenName(
                new HttpParameter[]{new HttpParameter("include_email", conf.isIncludeEmailEnabled())});
    }
    
    @Override
    public AccountSettings updateAccountSettings(final Integer trend_locationWoeid,
                                                 final Boolean sleep_timeEnabled, final String start_sleepTime,
                                                 final String end_sleepTime, final String time_zone, final String lang)
            throws TwitterException {
        List<HttpParameter> profile = new ArrayList<HttpParameter>(6);
        if (trend_locationWoeid != null) {
            profile.add(new HttpParameter("trend_location_woeid", trend_locationWoeid));
        }
        if (sleep_timeEnabled != null) {
            profile.add(new HttpParameter("sleep_time_enabled", sleep_timeEnabled.toString()));
        }
        if (start_sleepTime != null) {
            profile.add(new HttpParameter("start_sleep_time", start_sleepTime));
        }
        if (end_sleepTime != null) {
            profile.add(new HttpParameter("end_sleep_time", end_sleepTime));
        }
        if (time_zone != null) {
            profile.add(new HttpParameter("time_zone", time_zone));
        }
        if (lang != null) {
            profile.add(new HttpParameter("lang", lang));
        }
        return factory.createAccountSettings(post(conf.getRestBaseURL() + "account/settings.json"
, profile.toArray(new HttpParameter[profile.size()])));

    }

    @Override
    public AccountSettings updateAllowDmsFrom(final String allowDmsFrom) throws TwitterException {
        return factory.createAccountSettings(post(conf.getRestBaseURL() + "account/settings.json?allow_dms_from=" + allowDmsFrom));
    }

    @Override
    public User updateProfile(final String name, final String url
, final String location, final String description) throws TwitterException {
        List<HttpParameter> profile = new ArrayList<HttpParameter>(4);
        addParameterToList(profile, "name", name);
        addParameterToList(profile, "url", url);
        addParameterToList(profile, "location", location);
        addParameterToList(profile, "description", description);
        return factory.createUser(post(conf.getRestBaseURL() + "account/update_profile.json"
, profile.toArray(new HttpParameter[profile.size()])));
    }


    private void addParameterToList(final List<HttpParameter> colors,
                                    final String paramName, final String color) {
        if (color != null) {
            colors.add(new HttpParameter(paramName, color));
        }
    }

    @Override
    public User updateProfileImage(final File image) throws TwitterException {
        checkFileValidity(image);
        return factory.createUser(post(conf.getRestBaseURL()
                + "account/update_profile_image.json"
, new HttpParameter[]{new HttpParameter("image", image)}));
    }

    @Override
    public User updateProfileImage(final InputStream image) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL()
                + "account/update_profile_image.json"
, new HttpParameter[]{new HttpParameter("image", "image", image)}));
    }

    /**
     * Check the existence, and the type of the specified file.
     *
     * @param image image to be uploaded
     * @throws TwitterException when the specified file is not found (FileNotFoundException will be nested)
     *                          , or when the specified file object is not representing a file(IOException will be nested).
     */
    private void checkFileValidity(final File image) throws TwitterException {
        if (!image.exists()) {
            //noinspection ThrowableInstanceNeverThrown
            throw new TwitterException(new FileNotFoundException(image + " is not found."));
        }
        if (!image.isFile()) {
            //noinspection ThrowableInstanceNeverThrown
            throw new TwitterException(new IOException(image + " is not a file."));
        }
    }

    @Override
    public PagableResponseList<User> getBlocksList() throws
            TwitterException {
        return getBlocksList(-1L);
    }

    @Override
    public PagableResponseList<User> getBlocksList(final long cursor) throws
            TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "blocks/list.json?cursor=" + cursor));
    }

    @Override
    public IDs getBlocksIDs() throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "blocks/ids.json"));
    }

    @Override
    public IDs getBlocksIDs(final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "blocks/ids.json?cursor=" + cursor));
    }

    @Override
    public User createBlock(final long userId) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "blocks/create.json?user_id=" + userId));
    }

    @Override
    public User createBlock(final String screenName) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "blocks/create.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName)
                }
        ));
    }

    @Override
    public User destroyBlock(final long userId) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "blocks/destroy.json?user_id=" + userId));
    }

    @Override
    public User destroyBlock(final String screenName) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "blocks/destroy.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName),
                }
        ));
    }


    @Override
    public PagableResponseList<User> getMutesList(final long cursor) throws
            TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "mutes/users/list.json?cursor=" + cursor));
    }

    @Override
    public IDs getMutesIDs(final long cursor) throws TwitterException {
        return factory.createIDs(get(conf.getRestBaseURL() + "mutes/users/ids.json?cursor=" + cursor));
    }

    @Override
    public User createMute(final long userId) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "mutes/users/create.json?user_id=" + userId));
    }

    @Override
    public User createMute(final String screenName) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "mutes/users/create.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName)
                }
        ));
    }

    @Override
    public User destroyMute(final long userId) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "mutes/users/destroy.json?user_id=" + userId));
    }

    @Override
    public User destroyMute(final String screenName) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "mutes/users/destroy.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName)
                }
        ));
    }

    @Override
    public ResponseList<User> lookupUsers(final long... ids) throws TwitterException {
        return factory.createUserList(get(conf.getRestBaseURL() + "users/lookup.json"
, new HttpParameter("user_id", StringUtil.join(ids))));
    }

    @Override
    public ResponseList<User> lookupUsers(final String... screenNames) throws TwitterException {
        return factory.createUserList(get(conf.getRestBaseURL() + "users/lookup.json"
, new HttpParameter("screen_name", StringUtil.join(screenNames))));
    }

    @Override
    public User showUser(final long userId) throws TwitterException {
        return factory.createUser(get(conf.getRestBaseURL() + "users/show.json?user_id=" + userId));
    }

    @Override
    public User showUser(final String screenName) throws TwitterException {
        return factory.createUser(get(conf.getRestBaseURL() + "users/show.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName)
                }
        ));
    }

    @Override
    public ResponseList<User> searchUsers(final String query, final int page) throws TwitterException {
        return factory.createUserList(get(conf.getRestBaseURL() + "users/search.json"
, new HttpParameter("q", query), new HttpParameter("per_page", 20)
, new HttpParameter("page", page)));
    }

    @Override
    public ResponseList<User> getContributees(final long userId) throws TwitterException {
        return factory.createUserList(get(conf.getRestBaseURL() + "users/contributees.json?user_id=" + userId));
    }

    @Override
    public ResponseList<User> getContributees(final String screenName) throws TwitterException {
        return factory.createUserList(get(conf.getRestBaseURL() + "users/contributees.json",
                new HttpParameter("screen_name", screenName)));
    }

    @Override
    public ResponseList<User> getContributors(final long userId) throws TwitterException {
        return factory.createUserList(get(conf.getRestBaseURL() + "users/contributors.json?user_id=" + userId));
    }

    @Override
    public ResponseList<User> getContributors(final String screenName) throws TwitterException {
        return factory.createUserList(get(conf.getRestBaseURL() + "users/contributors.json",
                new HttpParameter("screen_name", screenName)));
    }

    @Override
    public void removeProfileBanner() throws TwitterException {
        post(conf.getRestBaseURL()
                + "account/remove_profile_banner.json");
    }

    @Override
    public void updateProfileBanner(final File image) throws TwitterException {
        checkFileValidity(image);
        post(conf.getRestBaseURL()
                + "account/update_profile_banner.json"
, new HttpParameter("banner", image));
    }

    @Override
    public void updateProfileBanner(final InputStream image) throws TwitterException {
        post(conf.getRestBaseURL()
                + "account/update_profile_banner.json"
, new HttpParameter("banner", "banner", image));
    }

    /* Suggested Users Resources */

    @Override
    public ResponseList<User> getUserSuggestions(final String categorySlug) throws TwitterException {
        HttpResponse res;
        try {
            res = get(conf.getRestBaseURL() + "users/suggestions/" + URLEncoder.encode(categorySlug, "UTF-8") + ".json");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return factory.createUserListFromJSONArray_Users(res);
    }

    @Override
    public ResponseList<Category> getSuggestedUserCategories() throws TwitterException {
        return factory.createCategoryList(get(conf.getRestBaseURL() + "users/suggestions.json"));
    }

    @Override
    public ResponseList<User> getMemberSuggestions(final String categorySlug) throws TwitterException {
        HttpResponse res;
        try {
            res = get(conf.getRestBaseURL() + "users/suggestions/" + URLEncoder.encode(categorySlug, "UTF-8") + "/members.json");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return factory.createUserListFromJSONArray(res);
    }

    /* Favorites Resources */

    @Override
    public ResponseList<Status> getFavorites() throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/list.json"));
    }

    @Override
    public ResponseList<Status> getFavorites(final long userId) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/list.json?user_id=" + userId));
    }

    @Override
    public ResponseList<Status> getFavorites(final String screenName) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/list.json",
                new HttpParameter("screen_name", screenName)));
    }

    @Override
    public ResponseList<Status> getFavorites(final Paging paging) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/list.json", paging.asPostParameterArray()));
    }

    @Override
    public ResponseList<Status> getFavorites(final long userId, final Paging paging) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/list.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("user_id", userId)}
, paging.asPostParameterArray())
        ));
    }

    @Override
    public ResponseList<Status> getFavorites(final String screenName, final Paging paging) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "favorites/list.json",
                mergeParameters(new HttpParameter[]{new HttpParameter("screen_name", screenName)}
, paging.asPostParameterArray())
        ));
    }

    @Override
    public Status destroyFavorite(final long id) throws TwitterException {
        return factory.createStatus(post(conf.getRestBaseURL() + "favorites/destroy.json?id=" + id));
    }

    @Override
    public Status createFavorite(final long id) throws TwitterException {
        return factory.createStatus(post(conf.getRestBaseURL() + "favorites/create.json?id=" + id));
    }

    /* Lists Resources */

    @Override
    public ResponseList<UserList> getUserLists(final String listOwnerScreenName) throws TwitterException {
        return getUserLists(listOwnerScreenName, false);
    }

    @Override
    public ResponseList<UserList> getUserLists(final String listOwnerScreenName, final boolean reverse) throws TwitterException {
        return factory.createUserListList(get(conf.getRestBaseURL() + "lists/list.json",
                new HttpParameter("screen_name", listOwnerScreenName),
                new HttpParameter("reverse", reverse)));
    }

    @Override
    public ResponseList<UserList> getUserLists(final long listOwnerUserId) throws TwitterException {
        return getUserLists(listOwnerUserId, false);
    }

    @Override
    public ResponseList<UserList> getUserLists(final long listOwnerUserId, final boolean reverse) throws TwitterException {
        return factory.createUserListList(get(conf.getRestBaseURL() + "lists/list.json",
                new HttpParameter("user_id", listOwnerUserId),
                new HttpParameter("reverse", reverse)));
    }

    @Override
    public ResponseList<Status> getUserListStatuses(final long listId, final Paging paging) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "lists/statuses.json"
, mergeParameters(paging.asPostParameterArray(Paging.SMCP, Paging.COUNT)
, new HttpParameter("list_id", listId))));
    }

    @Override
    public ResponseList<Status> getUserListStatuses(final long ownerId, final String slug, final Paging paging) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "lists/statuses.json"
, mergeParameters(paging.asPostParameterArray(Paging.SMCP, Paging.COUNT)
, new HttpParameter[]{new HttpParameter("owner_id", ownerId)
, new HttpParameter("slug", slug)})));
    }

    @Override
    public ResponseList<Status> getUserListStatuses(final String ownerScreenName,
                                                    final String slug, final Paging paging) throws TwitterException {
        return factory.createStatusList(get(conf.getRestBaseURL() + "lists/statuses.json"
, mergeParameters(paging.asPostParameterArray(Paging.SMCP, Paging.COUNT)
, new HttpParameter[]{new HttpParameter("owner_screen_name", ownerScreenName)
, new HttpParameter("slug", slug)})));
    }

    @Override
    public UserList destroyUserListMember(final long listId, final long userId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                        + "lists/members/destroy.json",
                new HttpParameter[]{new HttpParameter("list_id", listId), new HttpParameter("user_id", userId)}
        ));
    }

    @Override
    public UserList destroyUserListMember(final long ownerId, final String slug, final long userId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/destroy.json", new HttpParameter[]{new HttpParameter("owner_id", ownerId)
, new HttpParameter("slug", slug), new HttpParameter("user_id", userId)}));
    }

    @Override
    public UserList destroyUserListMember(final long listId, final String screenName) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/destroy.json", new HttpParameter[]{new HttpParameter("list_id", listId),
                new HttpParameter("screen_name", screenName)}));
    }

    @Override
    public UserList destroyUserListMember(final String ownerScreenName, final String slug,
                                          final long userId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/destroy.json", new HttpParameter[]{new HttpParameter("owner_screen_name", ownerScreenName)
, new HttpParameter("slug", slug), new HttpParameter("user_id", userId)}));
    }

    @Override
    public UserList destroyUserListMembers(final long listId, final String[] screenNames) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/destroy_all.json", new HttpParameter[]{new HttpParameter("list_id", listId),
                new HttpParameter("screen_name", StringUtil.join(screenNames))}));
    }

    @Override
    public UserList destroyUserListMembers(final long listId, final long[] userIds) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/destroy_all.json", new HttpParameter[]{new HttpParameter("list_id", listId),
                new HttpParameter("user_id", StringUtil.join(userIds))}));
    }

    @Override
    public UserList destroyUserListMembers(final String ownerScreenName, final String slug, final String[] screenNames) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/destroy_all.json", new HttpParameter[]{new HttpParameter("owner_screen_name", ownerScreenName),
                new HttpParameter("slug", slug),
                new HttpParameter("screen_name", StringUtil.join(screenNames))}));
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final long cursor) throws TwitterException {
        return getUserListMemberships(20, cursor);
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final int count, final long cursor) throws TwitterException {
        return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/memberships.json",
          new HttpParameter("cursor", cursor),
          new HttpParameter("count", count)));
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final String listMemberScreenName, final long cursor) throws TwitterException {
        return getUserListMemberships(listMemberScreenName, cursor, false);
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final String listMemberScreenName, final int count, final long cursor) throws TwitterException {
        return getUserListMemberships(listMemberScreenName, count, cursor, false);
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final String listMemberScreenName, final long cursor, final boolean filterToOwnedLists) throws TwitterException {
        return getUserListMemberships(listMemberScreenName, 20, cursor, filterToOwnedLists);
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final String listMemberScreenName, final int count, final long cursor, final boolean filterToOwnedLists) throws TwitterException {
        return factory.createPagableUserListList(get(conf.getRestBaseURL()
                + "lists/memberships.json",
          new HttpParameter("screen_name", listMemberScreenName),
          new HttpParameter("count", count),
          new HttpParameter("cursor", cursor),
          new HttpParameter("filter_to_owned_lists", filterToOwnedLists)));
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final long listMemberId, final long cursor) throws TwitterException {
        return getUserListMemberships(listMemberId, cursor, false);
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final long listMemberId, final int count, final long cursor) throws TwitterException {
        return getUserListMemberships(listMemberId, count, cursor, false);
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final long listMemberId, final long cursor, final boolean filterToOwnedLists) throws TwitterException {
        return getUserListMemberships(listMemberId, 20, cursor, filterToOwnedLists);
    }

    @Override
    public PagableResponseList<UserList> getUserListMemberships(final long listMemberId, final int count, final long cursor, final boolean filterToOwnedLists) throws TwitterException {
        return factory.createPagableUserListList(get(conf.getRestBaseURL()
                + "lists/memberships.json",
                new HttpParameter("user_id", listMemberId),
                new HttpParameter("count", count),
                new HttpParameter("cursor", cursor),
                new HttpParameter("filter_to_owned_lists", filterToOwnedLists)));
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(final long listId, final long cursor) throws TwitterException {
        return getUserListSubscribers(listId, 20, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(final long listId, final int count, final long cursor) throws TwitterException {
        return getUserListSubscribers(listId, count, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(final long listId, final int count, final long cursor, final boolean skipStatus) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "lists/subscribers.json",
          new HttpParameter("list_id", listId),
          new HttpParameter("count", count),
          new HttpParameter("cursor", cursor),
          new HttpParameter("skip_status", skipStatus)));
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(final long ownerId, final String slug, final long cursor) throws TwitterException {
        return getUserListSubscribers(ownerId, slug, 20, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(final long ownerId, final String slug, final int count, final long cursor) throws TwitterException {
        return getUserListSubscribers(ownerId, slug, count, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(final long ownerId, final String slug, final int count, final long cursor, final boolean skipStatus) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "lists/subscribers.json",
          new HttpParameter("owner_id", ownerId),
          new HttpParameter("slug", slug),
          new HttpParameter("count", count),
          new HttpParameter("cursor", cursor),
          new HttpParameter("skip_status", skipStatus)));
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(final String ownerScreenName, final String slug, final long cursor) throws TwitterException {
        return getUserListSubscribers(ownerScreenName, slug, 20, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(final String ownerScreenName, final String slug, final int count, final long cursor) throws TwitterException {
        return getUserListSubscribers(ownerScreenName, slug, count, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListSubscribers(
            final String ownerScreenName, final String slug, final int count, final long cursor, final boolean skipStatus)
            throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "lists/subscribers.json",
                new HttpParameter("owner_screen_name", ownerScreenName),
                new HttpParameter("slug", slug),
                new HttpParameter("count", count),
                new HttpParameter("cursor", cursor),
                new HttpParameter("skip_status", skipStatus)));
    }

    @Override
    public UserList createUserListSubscription(final long listId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/subscribers/create.json", new HttpParameter[]{new HttpParameter("list_id", listId)}));
    }

    @Override
    public UserList createUserListSubscription(final long ownerId, final String slug) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/subscribers/create.json", new HttpParameter[]{new HttpParameter("owner_id", ownerId)
, new HttpParameter("slug", slug)}));
    }


    @Override
    public UserList createUserListSubscription(final String ownerScreenName,
                                               final String slug) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/subscribers/create.json", new HttpParameter[]{new HttpParameter("owner_screen_name", ownerScreenName)
, new HttpParameter("slug", slug)}));
    }

    @Override
    public User showUserListSubscription(final long listId, final long userId) throws TwitterException {
        return factory.createUser(get(conf.getRestBaseURL()
                + "lists/subscribers/show.json?list_id=" + listId + "&user_id=" + userId));
    }

    @Override
    public User showUserListSubscription(final long ownerId, final String slug, final long userId) throws TwitterException {
        return factory.createUser(get(conf.getRestBaseURL()
                + "lists/subscribers/show.json?owner_id=" + ownerId + "&slug=" + slug + "&user_id=" + userId));
    }

    @Override
    public User showUserListSubscription(final String ownerScreenName, final String slug,
                                         final long userId) throws TwitterException {
        return factory.createUser(get(conf.getRestBaseURL()
                + "lists/subscribers/show.json",
                new HttpParameter[]{
                        new HttpParameter("owner_screen_name", ownerScreenName),
                        new HttpParameter("slug", slug),
                        new HttpParameter("user_id", userId)
                }
        ));
    }

    @Override
    public UserList destroyUserListSubscription(final long listId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/subscribers/destroy.json", new HttpParameter[]{new HttpParameter("list_id", listId)}));
    }

    @Override
    public UserList destroyUserListSubscription(final long ownerId, final String slug) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/subscribers/destroy.json"
, new HttpParameter[]{new HttpParameter("owner_id", ownerId), new HttpParameter("slug", slug)}));
    }

    @Override
    public UserList destroyUserListSubscription(final String ownerScreenName,
                                                final String slug) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/subscribers/destroy.json"
, new HttpParameter[]{new HttpParameter("owner_screen_name", ownerScreenName), new HttpParameter("slug", slug)}));
    }

    @Override
    public UserList createUserListMembers(final long listId, final long... userIds) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/members/create_all.json",
                new HttpParameter[]{new HttpParameter("list_id", listId), new HttpParameter("user_id"
, StringUtil.join(userIds))}
        ));
    }

    @Override
    public UserList createUserListMembers(final long ownerId, final String slug, final long... userIds) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/members/create_all.json",
                new HttpParameter[]{new HttpParameter("owner_id", ownerId), new HttpParameter("slug", slug)
, new HttpParameter("user_id", StringUtil.join(userIds))}
        ));
    }

    @Override
    public UserList createUserListMembers(final String ownerScreenName, final String slug,
                                          final long... userIds) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/members/create_all.json",
                new HttpParameter[]{new HttpParameter("owner_screen_name", ownerScreenName), new HttpParameter("slug", slug)
, new HttpParameter("user_id", StringUtil.join(userIds))}
        ));
    }

    @Override
    public UserList createUserListMembers(final long listId, final String... screenNames) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                        + "lists/members/create_all.json",
                new HttpParameter[]{
                        new HttpParameter("list_id", listId),
                        new HttpParameter("screen_name", StringUtil.join(screenNames))}
        ));
    }

    @Override
    public UserList createUserListMembers(final long ownerId, final String slug, final String... screenNames) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                        + "lists/members/create_all.json",
                new HttpParameter[]{new HttpParameter("owner_id", ownerId), new HttpParameter("slug", slug)
, new HttpParameter("screen_name", StringUtil.join(screenNames))}
        ));
    }

    @Override
    public UserList createUserListMembers(final String ownerScreenName, final String slug,
                                          final String... screenNames) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                        + "lists/members/create_all.json",
                new HttpParameter[]{new HttpParameter("owner_screen_name", ownerScreenName), new HttpParameter("slug", slug)
, new HttpParameter("screen_name", StringUtil.join(screenNames))}
        ));
    }

    @Override
    public User showUserListMembership(final long listId, final long userId) throws TwitterException {
        return factory.createUser(get(conf.getRestBaseURL()
                + "lists/members/show.json?list_id=" + listId + "&user_id=" + userId));
    }

    @Override
    public User showUserListMembership(final long ownerId, final String slug, final long userId) throws TwitterException {
        return factory.createUser(get(conf.getRestBaseURL()
                + "lists/members/show.json?owner_id=" + ownerId + "&slug=" + slug + "&user_id=" + userId));
    }

    @Override
    public User showUserListMembership(final String ownerScreenName, final String slug,
                                       final long userId) throws TwitterException {
        return factory.createUser(get(conf.getRestBaseURL()
                + "lists/members/show.json",
                new HttpParameter[]{
                        new HttpParameter("owner_screen_name", ownerScreenName),
                        new HttpParameter("slug", slug),
                        new HttpParameter("user_id", userId)
                }
        ));
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final long listId
, final long cursor) throws TwitterException {
        return getUserListMembers(listId, 20, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final long listId, final int count, final long cursor) throws TwitterException {
        return getUserListMembers(listId, count, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final long listId, final int count, final long cursor, final boolean skipStatus) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL() + "lists/members.json",
          new HttpParameter("list_id", listId),
          new HttpParameter("count", count),
          new HttpParameter("cursor", cursor),
          new HttpParameter("skip_status", skipStatus)));
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final long ownerId, final String slug, final long cursor) throws TwitterException {
        return getUserListMembers(ownerId, slug, 20, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final long ownerId, final String slug, final int count, final long cursor) throws TwitterException {
        return getUserListMembers(ownerId, slug, count, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final long ownerId, final String slug, final int count, final long cursor, final boolean skipStatus) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL()
                + "lists/members.json",
          new HttpParameter("owner_id", ownerId),
          new HttpParameter("slug", slug),
          new HttpParameter("count", count),
          new HttpParameter("cursor", cursor),
          new HttpParameter("skip_status", skipStatus)));
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final String ownerScreenName, final String slug, final long cursor) throws TwitterException {
      return getUserListMembers(ownerScreenName, slug, 20, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final String ownerScreenName, final String slug, final int count, final long cursor) throws TwitterException {
      return getUserListMembers(ownerScreenName, slug, count, cursor, false);
    }

    @Override
    public PagableResponseList<User> getUserListMembers(final String ownerScreenName,
                                                        final String slug, final int count, final long cursor, final boolean skipStatus) throws TwitterException {
        return factory.createPagableUserList(get(conf.getRestBaseURL()
                + "lists/members.json",
                new HttpParameter("owner_screen_name", ownerScreenName),
                new HttpParameter("slug", slug),
                new HttpParameter("count", count),
                new HttpParameter("cursor", cursor),
                new HttpParameter("skip_status", skipStatus)));
    }

    @Override
    public UserList createUserListMember(final long listId, final long userId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/create.json", new HttpParameter[]{new HttpParameter("user_id", userId)
, new HttpParameter("list_id", listId)}));
    }

    @Override
    public UserList createUserListMember(final long ownerId, final String slug, final long userId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/create.json", new HttpParameter[]{new HttpParameter("user_id", userId)
, new HttpParameter("owner_id", ownerId), new HttpParameter("slug", slug)}));
    }

    @Override
    public UserList createUserListMember(final String ownerScreenName, final String slug,
                                         final long userId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL()
                + "lists/members/create.json", new HttpParameter[]{new HttpParameter("user_id", userId)
, new HttpParameter("owner_screen_name", ownerScreenName), new HttpParameter("slug", slug)}));
    }

    @Override
    public UserList destroyUserList(final long listId) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/destroy.json",
                new HttpParameter[]{new HttpParameter("list_id", listId)}));
    }

    @Override
    public UserList destroyUserList(final long ownerId, final String slug) throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/destroy.json",
                new HttpParameter[]{new HttpParameter("owner_id", ownerId)
, new HttpParameter("slug", slug)}
        ));
    }

    @Override
    public UserList destroyUserList(final String ownerScreenName, final String slug)
            throws TwitterException {
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/destroy.json",
                new HttpParameter[]{new HttpParameter("owner_screen_name", ownerScreenName)
, new HttpParameter("slug", slug)}
        ));
    }

    @Override
    public UserList updateUserList(final long listId, final String newListName, final boolean isPublicList, final String newDescription) throws TwitterException {
        return updateUserList(newListName, isPublicList, newDescription, new HttpParameter("list_id", listId));
    }

    @Override
    public UserList updateUserList(final long ownerId, final String slug, final String newListName, final boolean isPublicList, final String newDescription) throws TwitterException {
        return updateUserList(newListName, isPublicList, newDescription, new HttpParameter("owner_id", ownerId)
, new HttpParameter("slug", slug));
    }

    @Override
    public UserList updateUserList(final String ownerScreenName, final String slug,
                                   final String newListName, final boolean isPublicList, final String newDescription)
            throws TwitterException {
        return updateUserList(newListName, isPublicList, newDescription, new HttpParameter("owner_screen_name", ownerScreenName)
, new HttpParameter("slug", slug));
    }

    private UserList updateUserList(final String newListName, final boolean isPublicList, final String newDescription, final HttpParameter... params) throws TwitterException {
        List<HttpParameter> httpParams = new ArrayList<HttpParameter>();
        Collections.addAll(httpParams, params);
        if (newListName != null) {
            httpParams.add(new HttpParameter("name", newListName));
        }
        httpParams.add(new HttpParameter("mode", isPublicList ? "public" : "private"));
        if (newDescription != null) {
            httpParams.add(new HttpParameter("description", newDescription));
        }
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/update.json", httpParams.toArray(new HttpParameter[httpParams.size()])));
    }

    @Override
    public UserList createUserList(final String listName, final boolean isPublicList, final String description) throws TwitterException {
        List<HttpParameter> httpParams = new ArrayList<HttpParameter>();
        httpParams.add(new HttpParameter("name", listName));
        httpParams.add(new HttpParameter("mode", isPublicList ? "public" : "private"));
        if (description != null) {
            httpParams.add(new HttpParameter("description", description));
        }
        return factory.createAUserList(post(conf.getRestBaseURL() + "lists/create.json",
                httpParams.toArray(new HttpParameter[httpParams.size()])));
    }

    @Override
    public UserList showUserList(final long listId) throws TwitterException {
        return factory.createAUserList(get(conf.getRestBaseURL() + "lists/show.json?list_id=" + listId));
    }

    @Override
    public UserList showUserList(final long ownerId, final String slug) throws TwitterException {
        return factory.createAUserList(get(conf.getRestBaseURL() + "lists/show.json?owner_id=" + ownerId + "&slug="
                + slug));
    }

    @Override
    public UserList showUserList(final String ownerScreenName, final String slug)
            throws TwitterException {
        return factory.createAUserList(get(conf.getRestBaseURL() + "lists/show.json",
                new HttpParameter[]{
                        new HttpParameter("owner_screen_name", ownerScreenName),
                        new HttpParameter("slug", slug)
                }
        ));
    }

    @Override
    public PagableResponseList<UserList> getUserListSubscriptions(final String listSubscriberScreenName, final long cursor) throws TwitterException {
        return getUserListSubscriptions(listSubscriberScreenName, 20, cursor);
    }

    @Override
    public PagableResponseList<UserList> getUserListSubscriptions(final String listSubscriberScreenName, final int count, final long cursor) throws TwitterException {
        return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/subscriptions.json",
                          new HttpParameter("screen_name", listSubscriberScreenName)
, new HttpParameter("count", count)
, new HttpParameter("cursor", cursor)));
    }

    @Override
    public PagableResponseList<UserList> getUserListSubscriptions(final long listSubscriberId, final long cursor) throws TwitterException {
        return getUserListSubscriptions(listSubscriberId, 20, cursor);
    }

    @Override
    public PagableResponseList<UserList> getUserListSubscriptions(final long listSubscriberId, final int count, final long cursor) throws TwitterException {
        return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/subscriptions.json",
                          new HttpParameter("user_id", listSubscriberId),
                          new HttpParameter("count", count),
                          new HttpParameter("cursor", cursor)));
    }

    public PagableResponseList<UserList> getUserListsOwnerships(final String listOwnerScreenName, final long cursor) throws TwitterException {
      return getUserListsOwnerships(listOwnerScreenName, 20, cursor);
    }

    @Override
    public PagableResponseList<UserList> getUserListsOwnerships(final String listOwnerScreenName, final int count, final long cursor) throws TwitterException {
        return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/ownerships.json",
                new HttpParameter("screen_name", listOwnerScreenName)
, new HttpParameter("count", count)
, new HttpParameter("cursor", cursor)));
    }

    @Override
    public PagableResponseList<UserList> getUserListsOwnerships(final long listOwnerId, final long cursor) throws TwitterException {
      return getUserListsOwnerships(listOwnerId, 20, cursor);
    }

    @Override
    public PagableResponseList<UserList> getUserListsOwnerships(final long listOwnerId, final int count, final long cursor) throws TwitterException {
        return factory.createPagableUserListList(get(conf.getRestBaseURL() + "lists/ownerships.json",
                new HttpParameter("user_id", listOwnerId)
, new HttpParameter("count", count)
, new HttpParameter("cursor", cursor)));
    }

    /* Saved Searches Resources */

    @Override
    public ResponseList<SavedSearch> getSavedSearches() throws TwitterException {
        return factory.createSavedSearchList(get(conf.getRestBaseURL() + "saved_searches/list.json"));
    }

    @Override
    public SavedSearch showSavedSearch(final long id) throws TwitterException {
        return factory.createSavedSearch(get(conf.getRestBaseURL() + "saved_searches/show/" + id
                + ".json"));
    }

    @Override
    public SavedSearch createSavedSearch(final String query) throws TwitterException {
        return factory.createSavedSearch(post(conf.getRestBaseURL() + "saved_searches/create.json"
, new HttpParameter("query", query)));
    }

    @Override
    public SavedSearch destroySavedSearch(final long id) throws TwitterException {
        return factory.createSavedSearch(post(conf.getRestBaseURL()
                + "saved_searches/destroy/" + id + ".json"));
    }

    /* Places & Geo Resources */

    @Override
    public Place getGeoDetails(final String placeId) throws TwitterException {
        return factory.createPlace(get(conf.getRestBaseURL() + "geo/id/" + placeId
                + ".json"));
    }

    @Override
    public ResponseList<Place> reverseGeoCode(final GeoQuery query) throws TwitterException {
        try {
            return factory.createPlaceList(get(conf.getRestBaseURL()
                    + "geo/reverse_geocode.json", query.asHttpParameterArray()));
        } catch (TwitterException te) {
            if (te.getStatusCode() == 404) {
                return factory.createEmptyResponseList();
            } else {
                throw te;
            }
        }
    }

    @Override
    public ResponseList<Place> searchPlaces(final GeoQuery query) throws TwitterException {
        return factory.createPlaceList(get(conf.getRestBaseURL()
                + "geo/search.json", query.asHttpParameterArray()));
    }

    @Override
    public ResponseList<Place> getSimilarPlaces(final GeoLocation location, final String name, final String containedWithin, final String streetAddress) throws TwitterException {
        List<HttpParameter> params = new ArrayList<HttpParameter>(3);
        params.add(new HttpParameter("lat", location.getLatitude()));
        params.add(new HttpParameter("long", location.getLongitude()));
        params.add(new HttpParameter("name", name));
        if (containedWithin != null) {
            params.add(new HttpParameter("contained_within", containedWithin));
        }
        if (streetAddress != null) {
            params.add(new HttpParameter("attribute:street_address", streetAddress));
        }
        return factory.createPlaceList(get(conf.getRestBaseURL()
                + "geo/similar_places.json", params.toArray(new HttpParameter[params.size()])));
    }

    /* Trends Resources */

    @Override
    public Trends getPlaceTrends(final int woeid) throws TwitterException {
        return factory.createTrends(get(conf.getRestBaseURL()
                + "trends/place.json?id=" + woeid));
    }

    @Override
    public ResponseList<Location> getAvailableTrends() throws TwitterException {
        return factory.createLocationList(get(conf.getRestBaseURL()
                + "trends/available.json"));
    }

    @Override
    public ResponseList<Location> getClosestTrends(final GeoLocation location) throws TwitterException {
        return factory.createLocationList(get(conf.getRestBaseURL()
                        + "trends/closest.json",
                new HttpParameter("lat", location.getLatitude())
, new HttpParameter("long", location.getLongitude())));
    }

    /* Spam Reporting Resources */

    @Override
    public User reportSpam(final long userId) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "users/report_spam.json?user_id=" + userId));
    }

    @Override
    public User reportSpam(final String screenName) throws TwitterException {
        return factory.createUser(post(conf.getRestBaseURL() + "users/report_spam.json",
                new HttpParameter[]{
                        new HttpParameter("screen_name", screenName)
                }
        ));
    }

    /* Help Resources */

    @Override
    public TwitterAPIConfiguration getAPIConfiguration() throws TwitterException {
        return factory.createTwitterAPIConfiguration(get(conf.getRestBaseURL() + "help/configuration.json"));
    }

    @Override
    public ResponseList<Language> getLanguages() throws TwitterException {
        return factory.createLanguageList(get(conf.getRestBaseURL() + "help/languages.json"));
    }

    @Override
    public String getPrivacyPolicy() throws TwitterException {
        try {
            return get(conf.getRestBaseURL() + "help/privacy.json").asJSONObject().getString("privacy");
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }

    @Override
    public String getTermsOfService() throws TwitterException {
        try {
            return get(conf.getRestBaseURL() + "help/tos.json").asJSONObject().getString("tos");
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }

    @Override
    public Map<String, RateLimitStatus> getRateLimitStatus() throws TwitterException {
        return factory.createRateLimitStatuses(get(conf.getRestBaseURL() + "application/rate_limit_status.json"));
    }

    @Override
    public Map<String, RateLimitStatus> getRateLimitStatus(final String... resources) throws TwitterException {
        return factory.createRateLimitStatuses(get(conf.getRestBaseURL() + "application/rate_limit_status.json?resources=" + StringUtil.join(resources)));
    }

    @Override
    public TimelinesResources timelines() {
        return this;
    }

    @Override
    public TweetsResources tweets() {
        return this;
    }

    @Override
    public SearchResource search() {
        return this;
    }

    @Override
    public DirectMessagesResources directMessages() {
        return this;
    }

    @Override
    public FriendsFollowersResources friendsFollowers() {
        return this;
    }

    @Override
    public UsersResources users() {
        return this;
    }

    @Override
    public SuggestedUsersResources suggestedUsers() {
        return this;
    }

    @Override
    public FavoritesResources favorites() {
        return this;
    }

    @Override
    public ListsResources list() {
        return this;
    }

    @Override
    public SavedSearchesResources savedSearches() {
        return this;
    }

    @Override
    public PlacesGeoResources placesGeo() {
        return this;
    }

    @Override
    public TrendsResources trends() {
        return this;
    }

    @Override
    public SpamReportingResource spamReporting() {
        return this;
    }

    @Override
    public HelpResources help() {
        return this;
    }

    private HttpResponse get(final String url) throws TwitterException {
        ensureAuthorizationEnabled();
        if (IMPLICIT_PARAMS_STR.length() > 0) {
            if (url.contains("?")) {
                url = url + "&" + IMPLICIT_PARAMS_STR;
            } else {
                url = url + "?" + IMPLICIT_PARAMS_STR;
            }
        }
        if (!conf.isMBeanEnabled()) {
            return http.get(url, null, auth, this);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url, null, auth, this);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse get(final String url, final HttpParameter... params) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.get(url, mergeImplicitParams(params), auth, this);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url, mergeImplicitParams(params), auth, this);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse post(final String url) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.post(url, IMPLICIT_PARAMS, auth, this);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.post(url, IMPLICIT_PARAMS, auth, this);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse post(final String url, final HttpParameter... params) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.post(url, mergeImplicitParams(params), auth, this);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.post(url, mergeImplicitParams(params), auth, this);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse post(final String url, final JSONObject json) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.post(url, new HttpParameter[]{new HttpParameter(json)}, auth, this);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.post(url, new HttpParameter[]{new HttpParameter(json)}, auth, this);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpParameter[] mergeParameters(final HttpParameter[] params1, final HttpParameter[] params2) {
        if (params1 != null && params2 != null) {
            HttpParameter[] params = new HttpParameter[params1.length + params2.length];
            System.arraycopy(params1, 0, params, 0, params1.length);
            System.arraycopy(params2, 0, params, params1.length, params2.length);
            return params;
        }
        if (null == params1 && null == params2) {
            return new HttpParameter[0];
        }
        if (params1 != null) {
            return params1;
        } else {
            return params2;
        }
    }

    private HttpParameter[] mergeParameters(final HttpParameter[] params1, final HttpParameter params2) {
        if (params1 != null && params2 != null) {
            HttpParameter[] params = new HttpParameter[params1.length + 1];
            System.arraycopy(params1, 0, params, 0, params1.length);
            params[params.length - 1] = params2;
            return params;
        }
        if (null == params1 && null == params2) {
            return new HttpParameter[0];
        }
        if (params1 != null) {
            return params1;
        } else {
            return new HttpParameter[]{params2};
        }
    }

    private HttpParameter[] mergeImplicitParams(final HttpParameter... params) {
        return mergeParameters(params, IMPLICIT_PARAMS);
    }

    private boolean isOk(final HttpResponse response) {
        return response != null && response.getStatusCode() < 300;
    }

    @Override
    public String toString() {
        return "TwitterImpl{"
                + "INCLUDE_MY_RETWEET=" + INCLUDE_MY_RETWEET
                + '}';
    }
}
