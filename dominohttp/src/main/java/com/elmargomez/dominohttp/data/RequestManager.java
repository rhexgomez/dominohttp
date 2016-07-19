/*
 * Copyright 2016 Elmar Rhex Gomez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elmargomez.dominohttp.data;

import android.content.Context;
import android.os.Bundle;

import com.elmargomez.dominohttp.FileCache;
import com.elmargomez.dominohttp.RequestQueue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RequestManager {

    private static final String RESTORE_BUNDLE = "api_bundle_restoration";

    private Context mContext;
    private Object mBind;
    private RequestQueue mRequestQueue;
    private List<String> mPendingRequest;

    /**
     * Initialize our manager.
     *
     * @param context
     * @param bind
     * @param saveInstanceState
     * @return
     */
    public static RequestManager initialize(Context context, Object bind, Bundle saveInstanceState) {
        if (bind == null) {
            throw new IllegalArgumentException("The bind object must not be null!");
        }
        return new RequestManager(context, bind, saveInstanceState);
    }

    private RequestManager(Context context, Object bind, Bundle saveInstanceState) {
        mContext = context;
        mBind = bind;
        mRequestQueue = getRequestQueue();
        if (saveInstanceState != null) {
            mPendingRequest = saveInstanceState.getStringArrayList(RESTORE_BUNDLE);
        } else {
            mPendingRequest = new ArrayList<>();
        }
    }

    /**
     * Optional method if we wanted to restore the session. This method is good when you have
     * a pending request from the network and still waiting for it to arrive, It automatically
     * restores those request(s) without sending another one.
     *
     * @param bundle the restoration holder from
     *               {@link android.app.Activity#onSaveInstanceState(Bundle)}.
     */
    public void onSaveInstanceState(Bundle bundle) {
        if (mPendingRequest != null) {
            bundle.putStringArrayList(RESTORE_BUNDLE, (ArrayList) mPendingRequest);
        }
    }

    /**
     * Handles all the threading, networking and caching works. It is recommended to use
     * Singleton in part so that the work will be centralized.
     *
     * @return the worker Queue.
     */
    protected RequestQueue getRequestQueue() {
        return SingletonRequestQueue.initialize(mContext).getRequestQueue();
    }

    /**
     * Create and Start the request from the network.
     *
     * @param header the request header.
     * @param body   the request body binary.
     * @return
     */
    public WebRequest request(WebRequest.Header header, WebRequest.Body body) {
        return new WebRequest(mBind, header, body);
    }

    /**
     * Create a singleton instance of {@link RequestQueue}
     */
    private static class SingletonRequestQueue {

        private static SingletonRequestQueue instance;
        private RequestQueue requestQueue;

        private SingletonRequestQueue(Context context) {
            File cFile = new File(context.getCacheDir(), "Fcache");
            FileCache cache = new FileCache(cFile);
            requestQueue = new RequestQueue(cache);
        }

        public static SingletonRequestQueue initialize(Context context) {
            if (instance == null) {
                instance = new SingletonRequestQueue(context);
            }
            return instance;
        }

        public RequestQueue getRequestQueue() {
            return requestQueue;
        }

    }

}
