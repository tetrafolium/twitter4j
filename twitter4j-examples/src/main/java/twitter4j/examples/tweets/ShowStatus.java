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

package twitter4j.examples.tweets;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Shows one single status.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class ShowStatus {
    /**
     * Usage: java twitter4j.examples.tweets.ShowStatus [status id]
     *
     * @param args message
     */
    public static void main(final String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java twitter4j.examples.tweets.ShowStatus [status id]");
            System.exit(-1);
        }
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            Status status = twitter.showStatus(Long.parseLong(args[0]));
            System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to show status: " + te.getMessage());
            System.exit(-1);
        }
    }
}
