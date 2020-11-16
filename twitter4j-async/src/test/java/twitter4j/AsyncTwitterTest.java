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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import twitter4j.api.HelpResources;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
class AsyncTwitterTest extends TwitterTestBase implements TwitterListener {

    private AsyncTwitter async1 = null;
    private AsyncTwitter async2 = null;
    private AsyncTwitter async3 = null;
    private AsyncTwitter bestFriend1Async = null;
    private ResponseList<Location> locations;
    private ResponseList<Place> places;
    private Place place;
    private ResponseList<Category> categories;
    private AccountTotals totals;
    private AccountSettings settings;
    private ResponseList<Friendship> friendships;
    private ResponseList<UserList> userLists;
    private ResponseList<HelpResources.Language> languages;
    private TwitterAPIConfiguration apiConf;
    private SavedSearch savedSearch;
    private ResponseList<SavedSearch> savedSearches;
    private OEmbed oembed;

    private long twit4jblockID = 39771963L;
    private long id;

    @BeforeEach
    protected void beforeEach() throws Exception {
        super.beforeEach();
        AsyncTwitterFactory factory = new AsyncTwitterFactory(conf1);
        async1 = factory.getInstance();
        async1.addListener(this);

        async2 = new AsyncTwitterFactory(conf2).getInstance();
        async2.addListener(this);

        async3 = new AsyncTwitterFactory(conf3).getInstance();
        async3.addListener(this);

        bestFriend1Async = new AsyncTwitterFactory(bestFriend1Conf).getInstance();
        bestFriend1Async.addListener(this);

        statuses = null;
        users = null;
        messages = null;
        status = null;
        user = null;
        message = null;
        te = null;
    }

    @Test
    void testShowUser() throws Exception {
        async1.showUser(id1.screenName);
        waitForResponse();
        User user = this.user;
        assertEquals(id1.screenName, user.getScreenName());
        assertTrue(0 <= user.getFavouritesCount());
        assertTrue(0 <= user.getFollowersCount());
        assertTrue(0 <= user.getFriendsCount());
        assertTrue(0 <= user.getStatusesCount());
        assertNotNull(user.getProfileBackgroundColor());
        assertNotNull(user.getProfileTextColor());
        assertNotNull(user.getProfileLinkColor());
        assertNotNull(user.getProfileSidebarBorderColor());
        assertNotNull(user.getProfileSidebarFillColor());
        assertNotNull(user.getProfileTextColor());

        this.user = null;
    }

    @Test
    void testSearchUser() throws TwitterException {
        async1.searchUsers("Doug Williams", 1);
        waitForResponse();
        assertTrue(4 < users.size());
    }

    @Test
    void testGetUserTimeline_Show() throws Exception {
        async2.getUserTimeline();
        waitForResponse();
        assertTrue(10 < statuses.size(), "size");
        async2.getUserTimeline(new Paging(999383469L));
    }

    @Test
    void testAccountProfileImageUpdates() throws Exception {
        te = null;
        async1.updateProfileImage(getRandomlyChosenFile());
        waitForResponse();
        assertNull(te);
    }


    @Test
    void testFavorite() throws Exception {
        Status status = twitter1.getHomeTimeline().get(0);
        try {
            twitter2.destroyFavorite(status.getId());
        } catch (TwitterException ignored) {
        }
        async2.createFavorite(status.getId());
        waitForResponse();
        assertEquals(status, this.status);
        this.status = null;
        //need to wait for a second to get it destoryable
        Thread.sleep(5000);
        async2.destroyFavorite(status.getId());
        waitForResponse();
        if (te != null && te.getStatusCode() == 404) {
            // sometimes destroying favorite fails with 404
        } else {
            assertEquals(status, this.status);
        }
    }

    // disable test case for now due to the rate limitation
//
//
// @Test
// void testSocialGraphMethods() throws Exception {
//        async1.getFriendsIDs(-1);
//        waitForResponse();
//        int yusuke = 4933401;
//        assertIDExsits("twit4j is following yusuke", ids, yusuke);
//        int ryunosukey = 48528137;
//        async1.getFriendsIDs(ryunosukey, -1);
//        waitForResponse();
//        assertEquals("ryunosukey is not following anyone", 0, ids.getIDs().length);
//        async1.getFriendsIDs("yusuke", -1);
//        waitForResponse();
//        assertIDExsits("yusukey is following ryunosukey", ids, ryunosukey);
//
//        try {
//            twitter2.createFriendship(id1.screenName);
//        } catch (TwitterException te) {
//        }
//        async1.getFollowersIDs(-1);
//        waitForResponse();
//        assertIDExsits("twit4j2(6377362) is following twit4j(6358482)", ids, 6377362);
//        async1.getFollowersIDs(ryunosukey, -1);
//        waitForResponse();
//        assertIDExsits("yusukey is following ryunosukey", ids, yusuke);
//        async1.getFollowersIDs("ryunosukey", -1);
//        waitForResponse();
//        assertIDExsits("yusukey is following ryunosukey", ids, yusuke);
//    }

    private void assertIDExsits(final String assertion, final IDs ids, final int idToFind) {
        boolean found = false;
        for (long id : ids.getIDs()) {
            if (id == idToFind) {
                found = true;
                break;
            }
        }
        assertTrue(found, assertion);
    }

    @Test
    void testAccountMethods() throws Exception {

        async1.verifyCredentials();
        waitForResponse();
        assertNotNull(user);
        assertNotNull(user.getName());
        assertNotNull(user.getURL());
        assertNotNull(user.getLocation());
        assertNotNull(user.getDescription());

        String oldURL = user.getURL();

        String newName, newURL, newLocation, newDescription;
        newName = "name" + System.currentTimeMillis();
        newURL = "https://yusuke.blog/" + System.currentTimeMillis();
        newLocation = "location:" + System.currentTimeMillis();
        newDescription = "description:" + System.currentTimeMillis();

        async1.updateProfile(newName, newURL, newLocation, newDescription);

        waitForResponse();
        assertEquals(newName, user.getName());
        assertFalse(oldURL.equalsIgnoreCase(user.getURL()));
        assertEquals(newLocation, user.getLocation());
        assertEquals(newDescription, user.getDescription());

    }

    @Test
    void testShow() throws Exception {
        async2.showStatus(1000L);
        waitForResponse();
        assertEquals(52, status.getUser().getId());
        assertDeserializedFormIsEqual(status);
    }

    @Test
    void testBlock() throws Exception {
        async2.createBlock(id1.screenName);
        waitForResponse();
        async2.destroyBlock(id1.screenName);
        waitForResponse();

        async1.getBlocksList();
        waitForResponse();
        assertTrue(users.size() > 0);
        assertEquals(twit4jblockID, users.get(0).getId());
        async1.getBlocksList(-1L);
        waitForResponse();
        assertTrue(users.size() > 0);
        assertEquals(twit4jblockID, users.get(0).getId());
        async1.getBlocksIDs();
        waitForResponse();
        assertTrue(ids.getIDs().length > 0);
        assertEquals(twit4jblockID, ids.getIDs()[0]);
    }

    @Test
    void testMute() throws Exception {
        async2.createMute(id1.screenName);
        waitForResponse();
        async2.destroyMute(id1.screenName);
        waitForResponse();

        async1.getMutesList(-1L);
        waitForResponse();
        assertEquals(1, users.size());
        assertEquals(twit4jblockID, users.get(0).getId());
        async1.getMutesIDs(-1L);
        waitForResponse();
        assertEquals(1, ids.getIDs().length);
        assertEquals(twit4jblockID, ids.getIDs()[0]);
    }

    @Test
    void testUpdate() throws Exception {
        String date = new java.util.Date().toString() + "test";
        async1.updateStatus(date);
        waitForResponse();
        assertEquals(date, status.getText());

        long id = status.getId();

        async2.updateStatus(new StatusUpdate("@" + id1.screenName + " " + date).inReplyToStatusId(id));
        waitForResponse();
        assertEquals("@" + id1.screenName + " " + date, status.getText());
        assertEquals(id, status.getInReplyToStatusId());
        assertEquals(twitter1.verifyCredentials().getId(), status.getInReplyToUserId());


        id = status.getId();
        this.status = null;
        async2.destroyStatus(id);
        waitForResponse();
        assertEquals("@" + id1.screenName + " " + date, status.getText());
        assertDeserializedFormIsEqual(status);
    }

    @Test
    void testCreateDestroyFriend() throws Exception {
        async2.destroyFriendship(id1.screenName);
        waitForResponse();

//        twitterAPI2.destroyFriendshipAsync(id1.name);
//        waitForResponse();
//        assertEquals(403, te.getStatusCode());
        async2.createFriendship(id1.screenName, true);
        // the Twitter API is not returning appropriate notifications value
        // http://code.google.com/p/twitter-api/issues/detail?id=474
//        user detail = twitterAPI2.showUser(id1.name);
//        assertTrue(detail.isNotificationEnabled());
        waitForResponse();
        assertEquals(id1.screenName, user.getScreenName());

//        te = null;
//        twitterAPI2.createFriendshipAsync(id2.name);
//        waitForResponse();
//        assertEquals(403, te.getStatusCode());
        te = null;
        async2.createFriendship("doesnotexist--");
        waitForResponse();
        //now befriending with non-existing user returns 404
        //http://groups.google.com/group/twitter-development-talk/browse_thread/thread/bd2a912b181bc39f
        //assertEquals(404, te.getStatusCode());
        // now it returns 403
        assertEquals(403, te.getStatusCode());
        assertEquals(108, te.getErrorCode());

    }

    @Test
    void testRateLimitStatus() throws Exception {
        async1.getRateLimitStatus();
        waitForResponse();
        RateLimitStatus status = rateLimitStatus.values().iterator().next();
        assertTrue(1 < status.getLimit());
        assertTrue(1 < status.getRemaining());
    }

    @Test
    void testAppOnlyAuthWithBuildingConf1() throws Exception {
        // setup
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setApplicationOnlyAuthEnabled(true);
        final AsyncTwitter twitter = new AsyncTwitterFactory(builder.build()).getInstance();

        // exercise & verify
        twitter.setOAuthConsumer(browserConsumerKey, browserConsumerSecret);
        OAuth2Token token = twitter.getOAuth2Token();
        assertEquals("bearer", token.getTokenType());

        twitter.addListener(this);
        testRateLimitStatus();
    }

    @Test
    void testAppOnlyAuthAsyncWithBuildingConf1() throws Exception {
        // setup
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setApplicationOnlyAuthEnabled(true);
        final AsyncTwitter twitter = new AsyncTwitterFactory(builder.build()).getInstance();

        // exercise & verify
        twitter.setOAuthConsumer(browserConsumerKey, browserConsumerSecret);
        twitter.addListener(this);
        twitter.getOAuth2TokenAsync();
        waitForResponse();
        testRateLimitStatus();
    }

    @Test
    void testAppOnlyAuthWithBuildingConf2() throws Exception {
        // setup
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setApplicationOnlyAuthEnabled(true);
        builder.setOAuthConsumerKey(browserConsumerKey).setOAuthConsumerSecret(browserConsumerSecret);
        final AsyncTwitter twitter = new AsyncTwitterFactory(builder.build()).getInstance();

        // exercise & verify
        OAuth2Token token = twitter.getOAuth2Token();
        assertEquals("bearer", token.getTokenType());

        TwitterListener listener = new TwitterAdapter() {

            @Override
            public void gotRateLimitStatus(final Map<String, RateLimitStatus> rateLimitStatus) {
                super.gotRateLimitStatus(rateLimitStatus);
                RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");
                assertNotNull(searchTweetsRateLimit);
                assertEquals(searchTweetsRateLimit.getLimit(), 450);
                notifyResponse();
            }


            @Override
            public void onException(final TwitterException ex, final TwitterMethod method) {
                assertEquals(403, ex.getStatusCode());
                assertEquals(220, ex.getErrorCode());
                assertEquals("Your credentials do not allow access to this resource", ex.getErrorMessage());
                notifyResponse();
            }


        };
        twitter.addListener(listener);
        twitter.getRateLimitStatus("search");
        waitForResponse();

    }


    @Test
    void testAppOnlyAuthAsyncWithBuildingConf2() throws Exception {
        // setup
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setApplicationOnlyAuthEnabled(true);
        builder.setOAuthConsumerKey(browserConsumerKey).setOAuthConsumerSecret(browserConsumerSecret);
        final AsyncTwitter twitter = new AsyncTwitterFactory(builder.build()).getInstance();

        // exercise & verify
        twitter.addListener(this);
        twitter.getOAuth2TokenAsync();
        waitForResponse();

        TwitterListener listener = new TwitterAdapter() {

            @Override
            public void gotRateLimitStatus(final Map<String, RateLimitStatus> rateLimitStatus) {
                super.gotRateLimitStatus(rateLimitStatus);
                RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");
                assertNotNull(searchTweetsRateLimit);
                assertEquals(searchTweetsRateLimit.getLimit(), 450);
                notifyResponse();
            }


            @Override
            public void onException(final TwitterException ex, final TwitterMethod method) {
                assertEquals(403, ex.getStatusCode());
                assertEquals(220, ex.getErrorCode());
                assertEquals("Your credentials do not allow access to this resource", ex.getErrorMessage());
                notifyResponse();
            }


        };
        twitter.addListener(listener);
        twitter.getRateLimitStatus("search");
        waitForResponse();

    }

    @Test
    void testLookup() throws TwitterException {
        async1.lookup(20L, 432656548536401920L);
        waitForResponse();
        assertEquals(2, statuses.size());
    }


    private ResponseList<Status> statuses = null;
    private ResponseList<User> users = null;
    private ResponseList<DirectMessage> messages = null;
    private Status status = null;
    private User user = null;
    private boolean test;
    private UserList userList;
    private PagableResponseList<UserList> pagableUserLists;
    private Relationship relationship;
    private DirectMessage message = null;
    private TwitterException te = null;
    private Map<String, RateLimitStatus> rateLimitStatus;
    private boolean exists;
    private QueryResult queryResult;
    private IDs ids;
    private List<Trends> trendsList;
    private Trends trends;
    private boolean blockExists;

    /*Search API Methods*/
    @Override
    public void searched(final QueryResult result) {
        this.queryResult = result;
        notifyResponse();
    }

    /*Timeline Methods*/
    @Override
    public void gotHomeTimeline(final ResponseList<Status> statuses) {
        this.statuses = statuses;
        notifyResponse();
    }

    @Override
    public void gotUserTimeline(final ResponseList<Status> statuses) {
        this.statuses = statuses;
        notifyResponse();
    }

    @Override
    public void gotRetweetsOfMe(final ResponseList<Status> statuses) {
        this.statuses = statuses;
        notifyResponse();
    }

    @Override
    public void gotMentions(final ResponseList<Status> statuses) {
        this.statuses = statuses;
        notifyResponse();
    }

    @Override
    public void lookedup(final ResponseList<Status> statuses) {
        this.statuses = statuses;
        notifyResponse();
    }

    /*Status Methods*/
    @Override
    public void gotShowStatus(final Status status) {
        this.status = status;
        notifyResponse();
    }

    @Override
    public void updatedStatus(final Status status) {
        this.status = status;
        notifyResponse();
    }

    @Override
    public void destroyedStatus(final Status destroyedStatus) {
        this.status = destroyedStatus;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.0.10
     */
    @Override
    public void retweetedStatus(final Status retweetedStatus) {
        this.status = retweetedStatus;
        notifyResponse();
    }

    @Override
    public void gotOEmbed(final OEmbed oembed) {
        this.oembed = oembed;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.0
     */
    @Override
    public void gotRetweets(final ResponseList<Status> retweets) {
        this.statuses = retweets;
        notifyResponse();
    }

    /*User Methods*/
    @Override
    public void gotUserDetail(final User user) {
        this.user = user;
        notifyResponse();
    }

    @Override
    public void lookedupUsers(final ResponseList<User> users) {
        this.users = users;
        notifyResponse();
    }

    @Override
    public void searchedUser(final ResponseList<User> userList) {
        this.users = userList;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.1
     */
    @Override
    public void gotSuggestedUserCategories(final ResponseList<Category> categories) {
        this.categories = categories;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.1
     */
    @Override
    public void gotUserSuggestions(final ResponseList<User> users) {
        this.users = users;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.9
     */
    @Override
    public void gotMemberSuggestions(final ResponseList<User> users) {
        this.users = users;
        notifyResponse();
    }

    @Override
    public void gotContributors(final ResponseList<User> users) {
        notifyResponse();
    }

    @Override
    public void removedProfileBanner() {
        notifyResponse();
    }

    @Override
    public void updatedProfileBanner() {
        notifyResponse();
    }

    @Override
    public void gotContributees(final ResponseList<User> users) {
        notifyResponse();
    }

    /*List Methods*/

    @Override
    public void createdUserList(final UserList userList) {
        this.userList = userList;
        notifyResponse();
    }

    @Override
    public void updatedUserList(final UserList userList) {
        this.userList = userList;
        notifyResponse();
    }

    @Override
    public void gotUserLists(final ResponseList<UserList> userLists) {
        this.userLists = userLists;
        notifyResponse();
    }

    @Override
    public void gotShowUserList(final UserList userList) {
        this.userList = userList;
        notifyResponse();
    }

    @Override
    public void destroyedUserList(final UserList userList) {
        this.userList = userList;
        notifyResponse();
    }

    @Override
    public void gotUserListStatuses(final ResponseList<Status> statuses) {
        this.statuses = statuses;
        notifyResponse();
    }

    @Override
    public void gotUserListMemberships(final PagableResponseList<UserList> userLists) {
        this.pagableUserLists = userLists;
        notifyResponse();
    }

    @Override
    public void gotUserListSubscriptions(final PagableResponseList<UserList> userLists) {
        this.pagableUserLists = userLists;
        notifyResponse();
    }

    /*List Members Methods*/

    @Override
    public void gotUserListMembers(final PagableResponseList<User> users) {
        this.users = users;
        notifyResponse();
    }

    @Override
    public void gotSavedSearches(final ResponseList<SavedSearch> savedSearches) {
        this.savedSearches = savedSearches;
        notifyResponse();
    }

    @Override
    public void gotSavedSearch(final SavedSearch savedSearch) {
        this.savedSearch = savedSearch;
        notifyResponse();
    }

    @Override
    public void createdSavedSearch(final SavedSearch savedSearch) {
        this.savedSearch = savedSearch;
        notifyResponse();
    }

    @Override
    public void destroyedSavedSearch(final SavedSearch savedSearch) {
        this.savedSearch = savedSearch;
        notifyResponse();
    }

    @Override
    public void createdUserListMember(final UserList userList) {
        this.userList = userList;
    }

    @Override
    public void createdUserListMembers(final UserList userList) {
        this.userList = userList;
    }

    @Override
    public void destroyedUserListMember(final UserList userList) {
        this.userList = userList;
    }

    @Override
    public void checkedUserListMembership(final User user) {
        this.user = user;
    }

    /*List Subscribers Methods*/

    @Override
    public void gotUserListSubscribers(final PagableResponseList<User> users) {
        this.users = users;
    }

    @Override
    public void subscribedUserList(final UserList userList) {
        this.userList = userList;
    }

    @Override
    public void unsubscribedUserList(final UserList userList) {
        this.userList = userList;
    }

    @Override
    public void checkedUserListSubscription(final User user) {
        this.user = user;
    }

    /*Direct Message Methods*/
    @Override
    public void gotDirectMessages(final ResponseList<DirectMessage> messages) {
        this.messages = messages;
        notifyResponse();
    }

    @Override
    public void gotSentDirectMessages(final ResponseList<DirectMessage> messages) {
        this.messages = messages;
        notifyResponse();
    }

    @Override
    public void sentDirectMessage(final DirectMessage message) {
        this.message = message;
        notifyResponse();
    }

    @Override
    public void destroyedDirectMessage(final long id) {
        this.id = id;
        notifyResponse();
    }

    @Override
    public void gotDirectMessage(final DirectMessage message) {
        this.message = message;
        notifyResponse();
    }

    /*Friendship Methods*/
    @Override
    public void createdFriendship(final User user) {
        this.user = user;
        notifyResponse();
    }

    @Override
    public void destroyedFriendship(final User user) {
        this.user = user;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.0
     */
    @Override
    public void gotShowFriendship(final Relationship relationship) {
        this.relationship = relationship;
        notifyResponse();
    }

    @Override
    public void gotFriendsList(final PagableResponseList<User> users) {
        this.users = users;
        notifyResponse();
    }

    @Override
    public void gotFollowersList(final PagableResponseList<User> users) {
        this.users = users;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.2
     */
    @Override
    public void gotIncomingFriendships(final IDs ids) {
        this.ids = ids;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.2
     */
    @Override
    public void gotOutgoingFriendships(final IDs ids) {
        this.ids = ids;
        notifyResponse();
    }

    /*Social Graph Methods*/
    @Override
    public void gotFriendsIDs(final IDs ids) {
        this.ids = ids;
        notifyResponse();
    }

    @Override
    public void gotFollowersIDs(final IDs ids) {
        this.ids = ids;
        notifyResponse();
    }

    @Override
    public void lookedUpFriendships(final ResponseList<Friendship> friendships) {
        this.friendships = friendships;
        notifyResponse();
    }


    @Override
    public void updatedFriendship(final Relationship relationship) {
        this.relationship = relationship;
        notifyResponse();
    }

    /*Account Methods*/

    @Override
    public void gotRateLimitStatus(final Map<String, RateLimitStatus> rateLimitStatus) {
        this.rateLimitStatus = rateLimitStatus;
        notifyResponse();
    }

    @Override
    public void verifiedCredentials(final User user) {
        this.user = user;
        notifyResponse();
    }

    @Override
    public void gotAccountSettings(final AccountSettings settings) {
        this.settings = settings;
        notifyResponse();
    }

    @Override
    public void updatedAccountSettings(final AccountSettings settings) {
        this.settings = settings;
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.0
     */
    @Override
    public void updatedProfileImage(final User user) {
        this.user = user;
        notifyResponse();
    }


    @Override
    public void updatedProfile(final User user) {
        this.user = user;
        notifyResponse();
    }

    /*Favorite Methods*/
    @Override
    public void gotFavorites(final ResponseList<Status> statuses) {
        this.statuses = statuses;
        notifyResponse();
    }

    @Override
    public void createdFavorite(final Status status) {
        this.status = status;
        notifyResponse();
    }

    @Override
    public void destroyedFavorite(final Status status) {
        this.status = status;
        notifyResponse();
    }

    /*Block Methods*/
    @Override
    public void createdBlock(final User user) {
        this.user = user;
        notifyResponse();
    }

    @Override
    public void destroyedBlock(final User user) {
        this.user = user;
        notifyResponse();
    }

    @Override
    public void gotBlocksList(final ResponseList<User> blockingUsers) {
        this.users = blockingUsers;
        notifyResponse();
    }

    @Override
    public void gotBlockIDs(final IDs blockingUsersIDs) {
        this.ids = blockingUsersIDs;
        notifyResponse();
    }

    /*Mute Methods*/
    @Override
    public void createdMute(final User user) {
        this.user = user;
        notifyResponse();
    }

    @Override
    public void destroyedMute(final User user) {
        this.user = user;
        notifyResponse();
    }

    @Override
    public void gotMutesList(final ResponseList<User> mutingUsers) {
        this.users = mutingUsers;
        notifyResponse();
    }

    @Override
    public void gotMuteIDs(final IDs mutingUsersIDs) {
        this.ids = mutingUsersIDs;
        notifyResponse();
    }

    /*Spam Reporting Methods*/

    @Override
    public void reportedSpam(final User reportedSpammer) {
        this.user = reportedSpammer;
        notifyResponse();
    }

    /*Saved Searches Methods*/
    //getSavedSearches()
    //showSavedSearch()
    //createSavedSearch()
    //destroySavedSearch()

    /*Local Trends Methods*/

    /**
     * @param locations the locations
     * @since Twitter4J 2.1.1
     */
    @Override
    public void gotAvailableTrends(final ResponseList<Location> locations) {
        this.locations = locations;
        notifyResponse();
    }

    @Override
    public void gotClosestTrends(final ResponseList<Location> locations) {
        this.locations = locations;
        notifyResponse();
    }

    /*Geo Methods*/
    @Override
    public void searchedPlaces(final ResponseList<Place> places) {
        this.places = places;
        notifyResponse();
    }

    @Override
    public void gotSimilarPlaces(final ResponseList<Place> places) {
        this.places = places;
        notifyResponse();
    }

    @Override
    public void gotReverseGeoCode(final ResponseList<Place> places) {
        this.places = places;
        notifyResponse();
    }

    @Override
    public void gotGeoDetails(final Place place) {
        this.place = place;
        notifyResponse();
    }

    @Override
    public void gotPlaceTrends(final Trends trends) {
        this.trends = trends;
        notifyResponse();
    }

    /* Legal Resources */

    /**
     * @since Twitter4J 2.1.7
     */
    @Override
    public void gotTermsOfService(final String str) {
        notifyResponse();
    }

    /**
     * @since Twitter4J 2.1.7
     */
    @Override
    public void gotPrivacyPolicy(final String str) {
        notifyResponse();
    }

    /*Help Methods*/
    @Override
    public void gotAPIConfiguration(final TwitterAPIConfiguration conf) {
        this.apiConf = conf;
        notifyResponse();
    }

    @Override
    public void gotLanguages(final ResponseList<HelpResources.Language> languages) {
        this.languages = languages;
        notifyResponse();
    }

    /**
     * @param te     TwitterException
     * @param method int
     */
    @Override
    public void onException(final TwitterException te, final TwitterMethod method) {
        this.te = te;
        System.out.println("onexception on " + method.name());
        te.printStackTrace();
        notifyResponse();
    }

    @Override
    public void gotOAuthRequestToken(final RequestToken token) {
    }

    @Override
    public void gotOAuthAccessToken(final AccessToken token) {
    }

    @Override
    public void gotOAuth2Token(final OAuth2Token token) {
        System.out.println("[gotOAuth2Token] token:" + token.getAccessToken().replaceAll("\\w", "*") + " type:" + token.getTokenType());
        assertEquals("bearer", token.getTokenType());
        notifyResponse();
    }

    private synchronized void notifyResponse() {
        this.notify();
    }

    private synchronized void waitForResponse() {
        try {
            this.wait(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param obj the object to be asserted
     * @return the deserialized object
     * @throws Exception in the case the object is not (de)serializable
     */
    public static Object assertDeserializedFormIsEqual(final Object obj) throws Exception {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteOutputStream);
        oos.writeObject(obj);
        byteOutputStream.close();
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteOutputStream.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(byteInputStream);
        Object that = ois.readObject();
        byteInputStream.close();
        ois.close();
        assertEquals(obj, that);
        return that;
    }

    static final String[] files = {"src/test/resources/t4j-reverse.jpeg",
            "src/test/resources/t4j-reverse.png",
            "src/test/resources/t4j-reverse.gif",
            "src/test/resources/t4j.jpeg",
            "src/test/resources/t4j.png",
            "src/test/resources/t4j.gif",
    };

    private static File getRandomlyChosenFile() {
        int rand = (int) (System.currentTimeMillis() % 6);
        File file = new File(files[rand]);
        if (!file.exists()) {
            file = new File("twitter4j-core/" + files[rand]);
        }
        return file;
    }


}
