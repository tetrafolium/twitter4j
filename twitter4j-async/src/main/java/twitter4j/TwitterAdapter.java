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

import twitter4j.api.HelpResources;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.auth.RequestToken;

import java.util.Map;

/**
 * A handy adapter of TwitterListener.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see twitter4j.AsyncTwitter
 * @see twitter4j.TwitterListener
 */
public class TwitterAdapter implements TwitterListener {
    /* Timelines Resources */
    @Override
    public void gotMentions(final ResponseList<Status> statuses) {
    }

    @Override
    public void gotHomeTimeline(final ResponseList<Status> statuses) {
    }

    @Override
    public void gotUserTimeline(final ResponseList<Status> statuses) {
    }

    @Override
    public void gotRetweetsOfMe(final ResponseList<Status> statuses) {
    }

    /* Tweets Resources */
    @Override
    public void gotRetweets(final ResponseList<Status> retweets) {
    }

    @Override
    public void gotShowStatus(final Status status) {
    }

    @Override
    public void destroyedStatus(final Status destroyedStatus) {
    }

    @Override
    public void updatedStatus(final Status status) {
    }

    @Override
    public void retweetedStatus(final Status retweetedStatus) {
    }

    @Override
    public void gotOEmbed(final OEmbed oembed) {
    }

    @Override
    public void lookedup(final ResponseList<Status> statuses) {
    }

    /* Search Resources */
    @Override
    public void searched(final QueryResult queryResult) {
    }

    /* Direct Messages Resources */
    @Override
    public void gotDirectMessages(final ResponseList<DirectMessage> messages) {
    }

    @Override
    public void gotSentDirectMessages(final ResponseList<DirectMessage> messages) {
    }

    @Override
    public void gotDirectMessage(final DirectMessage message) {
    }

    @Override
    public void destroyedDirectMessage(final long id) {
    }

    @Override
    public void sentDirectMessage(final DirectMessage message) {
    }

    /* Friends & Followers Resources */
    @Override
    public void gotFriendsIDs(final IDs ids) {
    }

    @Override
    public void gotFollowersIDs(final IDs ids) {
    }

    @Override
    public void lookedUpFriendships(final ResponseList<Friendship> friendships) {
    }

    @Override
    public void gotIncomingFriendships(final IDs ids) {
    }

    @Override
    public void gotOutgoingFriendships(final IDs ids) {
    }

    @Override
    public void createdFriendship(final User user) {
    }

    @Override
    public void destroyedFriendship(final User user) {
    }

    @Override
    public void updatedFriendship(final Relationship relationship) {
    }

    @Override
    public void gotShowFriendship(final Relationship relationship) {
    }

    @Override
    public void gotFriendsList(final PagableResponseList<User> users) {
    }

    @Override
    public void gotFollowersList(final PagableResponseList<User> users) {
    }

    /* Users Resources */
    @Override
    public void gotAccountSettings(final AccountSettings settings) {
    }

    @Override
    public void verifiedCredentials(final User user) {
    }

    @Override
    public void updatedAccountSettings(final AccountSettings settings) {
    }

    // updatedDeliveryDevice
    @Override
    public void updatedProfile(final User user) {
    }

    @Override
    public void updatedProfileImage(final User user) {
    }

    @Override
    public void gotBlocksList(final ResponseList<User> blockingUsers) {
    }

    @Override
    public void gotBlockIDs(final IDs blockingUsersIDs) {
    }

    @Override
    public void createdBlock(final User user) {
    }

    @Override
    public void destroyedBlock(final User user) {
    }

    @Override
    public void lookedupUsers(final ResponseList<User> users) {
    }

    @Override
    public void gotUserDetail(final User user) {
    }

    @Override
    public void searchedUser(final ResponseList<User> userList) {
    }

    @Override
    public void gotContributees(final ResponseList<User> users) {
    }

    @Override
    public void gotContributors(final ResponseList<User> users) {
    }

    @Override
    public void removedProfileBanner() {
    }

    @Override
    public void updatedProfileBanner() {
    }

    @Override
    public void gotMutesList(final ResponseList<User> blockingUsers) {
    }

    @Override
    public void gotMuteIDs(final IDs blockingUsersIDs) {
    }

    @Override
    public void createdMute(final User user) {
    }

    @Override
    public void destroyedMute(final User user) {
    }

    /* Suggested Users Resources */
    @Override
    public void gotUserSuggestions(final ResponseList<User> users) {
    }

    @Override
    public void gotSuggestedUserCategories(final ResponseList<Category> category) {
    }

    @Override
    public void gotMemberSuggestions(final ResponseList<User> users) {
    }

    /* Favorites Resources */
    @Override
    public void gotFavorites(final ResponseList<Status> statuses) {
    }

    @Override
    public void createdFavorite(final Status status) {
    }

    @Override
    public void destroyedFavorite(final Status status) {
    }

    /* Lists Resources */
    @Override
    public void gotUserLists(final ResponseList<UserList> userLists) {
    }

    @Override
    public void gotUserListStatuses(final ResponseList<Status> statuses) {
    }

    @Override
    public void destroyedUserListMember(final UserList userList) {
    }

    @Override
    public void gotUserListMemberships(final PagableResponseList<UserList> userLists) {
    }

    @Override
    public void gotUserListSubscribers(final PagableResponseList<User> users) {
    }

    @Override
    public void subscribedUserList(final UserList userList) {
    }

    @Override
    public void checkedUserListSubscription(final User user) {
    }

    @Override
    public void unsubscribedUserList(final UserList userList) {
    }

    @Override
    public void createdUserListMembers(final UserList userList) {
    }

    @Override
    public void checkedUserListMembership(final User users) {
    }

    @Override
    public void createdUserListMember(final UserList userList) {
    }

    @Override
    public void destroyedUserList(final UserList userList) {
    }

    @Override
    public void updatedUserList(final UserList userList) {
    }

    @Override
    public void createdUserList(final UserList userList) {
    }

    @Override
    public void gotShowUserList(final UserList userList) {
    }

    @Override
    public void gotUserListSubscriptions(final PagableResponseList<UserList> userLists) {
    }

    @Override
    public void gotUserListMembers(final PagableResponseList<User> users) {
    }

    /* Saved Searches Resources */
    @Override
    public void gotSavedSearches(final ResponseList<SavedSearch> savedSearches) {
    }

    @Override
    public void gotSavedSearch(final SavedSearch savedSearch) {
    }

    @Override
    public void createdSavedSearch(final SavedSearch savedSearch) {
    }

    @Override
    public void destroyedSavedSearch(final SavedSearch savedSearch) {
    }

    /* Places & Geo Resources */
    @Override
    public void gotGeoDetails(final Place place) {
    }

    @Override
    public void gotReverseGeoCode(final ResponseList<Place> places) {
    }

    @Override
    public void searchedPlaces(final ResponseList<Place> places) {
    }

    @Override
    public void gotSimilarPlaces(final ResponseList<Place> places) {
    }

    /* Trends Resources */
    @Override
    public void gotPlaceTrends(final Trends trends) {
    }

    @Override
    public void gotAvailableTrends(final ResponseList<Location> locations) {
    }

    @Override
    public void gotClosestTrends(final ResponseList<Location> locations) {
    }

    /* Spam Reporting Resources */
    @Override
    public void reportedSpam(final User reportedSpammer) {
    }

    /* OAuth Resources */
    @Override
    public void gotOAuthRequestToken(final RequestToken token) {
    }

    @Override
    public void gotOAuthAccessToken(final AccessToken token) {
    }

    /* OAuth2 Resources */
    @Override
    public void gotOAuth2Token(final OAuth2Token token) {
    }

    /* Help Resources */
    @Override
    public void gotAPIConfiguration(final TwitterAPIConfiguration conf) {
    }

    @Override
    public void gotLanguages(final ResponseList<HelpResources.Language> languages) {
    }

    @Override
    public void gotPrivacyPolicy(final String privacyPolicy) {
    }

    @Override
    public void gotTermsOfService(final String tof) {
    }

    @Override
    public void gotRateLimitStatus(final Map<String, RateLimitStatus> rateLimitStatus) {
    }

    @Override
    public void onException(final TwitterException te, final TwitterMethod method) {
    }


}
