package net.fataled.wynnstacks.client.raids;

import net.fataled.wynnstacks.client.interfaces.RaidKind;

public class TCC implements RaidKind {

    @Override
    public String getEntryTitleRaw(){
        return "The Canyon Colossus";

    }
    @Override
    public String getRaidName(){
        return "The Canyon Colossus";
    }
}
