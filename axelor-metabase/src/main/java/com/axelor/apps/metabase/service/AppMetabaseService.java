/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.metabase.service;

import java.io.IOException;
import com.axelor.apps.base.db.AppMetabase;
import com.axelor.apps.base.db.repo.AppMetabaseRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import wslite.json.JSONException;
import wslite.json.JSONObject;

public class AppMetabaseService {

  @Inject private AppMetabaseRepository appMetabaseRepo;

  public String getReportServerUrl() throws JSONException, IOException {

    AppMetabase appMetabase = appMetabaseRepo.all().fetchOne();
    String serverUrl = appMetabase.getMetabaseServerUrl();
    if (serverUrl != null) {
      if (appMetabase == null || !appMetabase.getActive()) {
        return null;
      }
      User user = AuthUtils.getUser();

      if (user.getMetabaseEmailAddress() == null || user.getMetabasePassword() == null) {
        return null;
      }

      OkHttpClient client = new OkHttpClient();
      RequestBody requestBody =
          RequestBody.create(
              MediaType.parse("application/json; charset=utf-8"),
              "{\"username\":\""
                  + user.getMetabaseEmailAddress()
                  + "\",\"password\":\""
                  + user.getMetabasePassword()
                  + "\"}");

      Request request =
          new Request.Builder().url(serverUrl + "api/session").post(requestBody).build();

      Response response = client.newCall(request).execute();
      System.err.println(response.isSuccessful() + " : " + response.isRedirect());
      System.err.println(response.getClass()); 

      if (response.isSuccessful()) {
        JSONObject jsonObj = new JSONObject(response.body().string());
        System.err.println(jsonObj.get("id").toString());
        System.err.println(request.urlString());
        System.err.println(serverUrl ); 
        
        return serverUrl;
      }

      return null;
    }
    return null;
  }
}
