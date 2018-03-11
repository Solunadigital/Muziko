package com.muziko.api.Updater;

import com.muziko.common.models.UpdateModel;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Bradley on 21/02/2017.
 */

public interface UpdaterAPI {

	@GET("update.html")
	Call<UpdateModel> getVersion();

}