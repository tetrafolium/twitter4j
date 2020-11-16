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

import java.io.IOException;
import java.io.InputStream;

/**
 * StatusStream implementation. This class is NOT intended to be extended but left non-final for the ease of mock testing.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
class StatusStreamImpl extends StatusStreamBase {
    /*package*/

    StatusStreamImpl(final Dispatcher dispatcher, final InputStream stream, final Configuration conf) throws IOException {
        super(dispatcher, stream, conf);
    }
    /*package*/

    StatusStreamImpl(final Dispatcher dispatcher, final HttpResponse response, final Configuration conf) throws IOException {
        super(dispatcher, response, conf);
    }

    String line;

    static final RawStreamListener[] EMPTY = new RawStreamListener[0];

    @Override
    protected void onClose() { }

    @Override
    public void next(final StatusListener listener) throws TwitterException {
        handleNextElement(new StatusListener[]{listener}, EMPTY);
    }

    @Override
    public void next(final StreamListener[] listeners, final RawStreamListener[] rawStreamListeners) throws TwitterException {
        handleNextElement(listeners, rawStreamListeners);
    }

    @Override
    protected String parseLine(final String line) {
        this.line = line;
        return line;
    }

    @Override
    protected void onMessage(final String rawString, final RawStreamListener[] listeners) throws TwitterException {
        for (RawStreamListener listener : listeners) {
            listener.onMessage(rawString);
        }
    }

    @Override
    protected void onStatus(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
        for (StreamListener listener : listeners) {
            ((StatusListener) listener).onStatus(asStatus(json));
        }
    }

    @Override
    protected void onDelete(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        for (StreamListener listener : listeners) {
            JSONObject deletionNotice = json.getJSONObject("delete");
            if (deletionNotice.has("status")) {
                ((StatusListener) listener).onDeletionNotice(new StatusDeletionNoticeImpl(deletionNotice.getJSONObject("status")));
            } else {
                JSONObject directMessage = deletionNotice.getJSONObject("direct_message");
                ((UserStreamListener) listener).onDeletionNotice(ParseUtil.getLong("id", directMessage)
, ParseUtil.getLong("user_id", directMessage));
            }
        }
    }

    @Override
    protected void onLimit(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        for (StreamListener listener : listeners) {
            ((StatusListener) listener).onTrackLimitationNotice(ParseUtil.getInt("track", json.getJSONObject("limit")));
        }
    }

    @Override
    protected void onStallWarning(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        for (StreamListener listener : listeners) {
            ((StatusListener) listener).onStallWarning(new StallWarning(json));
        }
    }

    @Override
    protected void onScrubGeo(final JSONObject json, final StreamListener[] listeners) throws TwitterException, JSONException {
        JSONObject scrubGeo = json.getJSONObject("scrub_geo");
        for (StreamListener listener : listeners) {
            ((StatusListener) listener).onScrubGeo(ParseUtil.getLong("user_id", scrubGeo)
, ParseUtil.getLong("up_to_status_id", scrubGeo));
        }

    }

    @Override
    public void onException(final Exception e, final StreamListener[] listeners) {
        for (StreamListener listener : listeners) {
            listener.onException(e);
        }
    }
}
