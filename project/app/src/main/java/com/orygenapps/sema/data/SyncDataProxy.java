package com.orygenapps.sema.data;

import java.util.List;

/**
 * Created by ashemah on 20/04/15.
 */

public class SyncDataProxy {

    List<ProgramVersionProxy> program_versions;

    private String app_platform;
    private Integer app_build_number;
    private String app_version_string;
    private String push_token;

    SyncDataProxy(Integer buildNumber, String versionString, String pushToken, List<ProgramVersionProxy> versions) {

        this.app_platform = "android";
        this.app_build_number = buildNumber;
        this.app_version_string = versionString;
        this.push_token = pushToken;
        this.program_versions = versions;
    }

}
