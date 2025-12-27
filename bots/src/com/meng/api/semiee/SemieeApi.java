package com.meng.api.semiee;

import com.meng.tools.normal.JSON;
import com.meng.tools.normal.Network;

import java.io.IOException;

public class SemieeApi {

    public static final String API_SERVER = "https://www.semiee.com/bdxx-api/chip";

    public static SearchResult search(String keyword) throws IOException {
        return search(keyword, 0, 10);  
    }

    public static SearchResult search(String keyword, int index, int pageSize) throws IOException {
        return JSON.fromJson(Network.httpGet(API_SERVER + "/search?pageIndex=" + index + "&pageSize=" + pageSize + "&model=" + keyword), SearchResult.class);
    }

    public static ChipArticle getChipArticle(String chipId) throws IOException {
        return JSON.fromJson(Network.httpGet(API_SERVER + "/" + chipId + "/techniclarticle"), ChipArticle.class);
    }

    public static ChipDescription getChipDescription(String chipId) throws IOException {
        return JSON.fromJson(Network.httpGet(API_SERVER + "/detail/" + chipId), ChipDescription.class);
    }

    public static ChipInformation getChipInformation(String chipId) throws IOException {
        return JSON.fromJson(Network.httpGet(API_SERVER + "/" + chipId + "/encyclopedias"), ChipInformation.class);
    }

    public static ChipParameter getChipParameter(String chipId) throws IOException {
        return JSON.fromJson(Network.httpGet(API_SERVER + "/" + chipId + "/technicalparam"), ChipParameter.class);
    }

    public static ContactWay getContactWay(String chipId) throws IOException {
        return JSON.fromJson(Network.httpGet(API_SERVER + "/v2/" + chipId + "/contactWay"), ContactWay.class);
    }
}
