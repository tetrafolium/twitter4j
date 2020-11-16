package twitter4j;


/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
public class SiteStreamsAdapter implements SiteStreamsListener {
    @Override
    public void onStatus(final long forUser, final Status status) {
    }

    @Override
    public void onDeletionNotice(final long forUser, final StatusDeletionNotice statusDeletionNotice) {
    }

    @Override
    public void onFriendList(final long forUser, final long[] friendIds) {
    }

    @Override
    public void onFavorite(final long forUser, final User source, final User target, final Status favoritedStatus) {
    }

    @Override
    public void onUnfavorite(final long forUser, final User source, final User target, final Status unfavoritedStatus) {
    }

    @Override
    public void onFollow(final long forUser, final User source, final User followedUser) {
    }

    @Override
    public void onUnfollow(final long forUser, final User source, final User followedUser) {
    }

    @Override
    public void onDirectMessage(final long forUser, final DirectMessage directMessage) {
    }

    @Override
    public void onDeletionNotice(final long forUser, final long directMessageId, final long userId) {
    }

    @Override
    public void onUserListMemberAddition(final long forUser, final User addedUser, final User listOwner, final UserList list) {
    }

    @Override
    public void onUserListMemberDeletion(final long forUser, final User deletedUser, final User listOwner, final UserList list) {
    }

    @Override
    public void onUserListSubscription(final long forUser, final User subscriber, final User listOwner, final UserList list) {
    }

    @Override
    public void onUserListUnsubscription(final long forUser, final User subscriber, final User listOwner, final UserList list) {
    }

    @Override
    public void onUserListCreation(final long forUser, final User listOwner, final UserList list) {
    }

    @Override
    public void onUserListUpdate(final long forUser, final User listOwner, final UserList list) {
    }

    @Override
    public void onUserListDeletion(final long forUser, final User listOwner, final UserList list) {
    }

    @Override
    public void onUserProfileUpdate(final long forUser, final User updatedUser) {
    }

    @Override
    public void onUserSuspension(final long forUser, final long suspendedUser) {
    }

    @Override
    public void onUserDeletion(final long forUser, final long deletedUser) {
    }

    @Override
    public void onBlock(final long forUser, final User source, final User blockedUser) {
    }

    @Override
    public void onUnblock(final long forUser, final User source, final User unblockedUser) {
    }

    @Override
    public void onRetweetedRetweet(final User source, final User target, final Status retweetedStatus) {
    }

    @Override
    public void onFavoritedRetweet(final User source, final User target, final Status favoritedStatus) {
    }

    @Override
    public void onQuotedTweet(final User source, final User target, final Status quotingTweet) {
    }

    @Override
    public void onMute(final long forUser, final User source, final User target) {

    }

    @Override
    public void onUnmute(final long forUser, final User source, final User target) {

    }

    @Override
    public void onDisconnectionNotice(final String screenName) {
    }

    @Override
    public void onException(final Exception ex) {
    }
}
