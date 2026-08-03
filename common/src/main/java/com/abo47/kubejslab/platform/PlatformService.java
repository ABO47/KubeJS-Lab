package com.abo47.kubejslab.platform;

import java.nio.file.Path;

public interface PlatformService {
    Path configDir();

    String loaderName();

    String loaderVersion();

    String modVersion(String modId);
}
