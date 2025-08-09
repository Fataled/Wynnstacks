package net.fataled.wynnstacks.client.raids;

import net.fataled.wynnstacks.client.interfaces.RaidKind;

public class TNA implements RaidKind {

    @Override
    public String getEntryTitleRaw(){
        return "The Nameless Anomaly";
    }
    @Override
    public String getRaidName(){
        return "The Nameless Anomaly";
    }
}
