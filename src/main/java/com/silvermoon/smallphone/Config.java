package com.silvermoon.smallphone;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Hello World";

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration
            .getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "You have a new phone now!");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
