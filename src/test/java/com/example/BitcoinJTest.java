package com.example;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.*;
import org.bitcoinj.script.*;

import org.junit.jupiter.api.Test;

public class BitcoinJTest
{
    private static NetworkParameters params = MainNetParams.get();

    @Test
    public void test()
    {
        xpub("xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8", AddressFormat.BECH32);
    }

    enum AddressFormat
    {
        P2PKH, P2SH, BECH32,
    }

    private void xpub(String xpub, AddressFormat format)
    {
        DeterministicKey rootKey = DeterministicKey.deserializeB58(null, xpub, params);
        DeterministicHierarchy hierarchy = new DeterministicHierarchy(rootKey);

        DeterministicKey external = deriveNextChild(rootKey, hierarchy);
        derive(hierarchy, external, format);

        DeterministicKey change = deriveNextChild(rootKey, hierarchy);
        derive(hierarchy, change, format);
    }

    private void derive(DeterministicHierarchy hierarchy, DeterministicKey chain, AddressFormat format)
    {
        for (int i = 0; i < 10; i++)
        {
            DeterministicKey key = deriveNextChild(chain, hierarchy);
            Address address = getAddress(key, format);
            System.out.println(key.getPath() + ": " + address);
        }
    }

    private DeterministicKey deriveNextChild(DeterministicKey rootKey, DeterministicHierarchy hierarchy)
    {
        return hierarchy.deriveNextChild(rootKey.getPath(), false, false, false);
    }

    private Address getAddress(ECKey key, AddressFormat format)
    {
        switch (format)
        {
        case P2PKH:
            return LegacyAddress.fromPubKeyHash(params, key.getPubKeyHash());
        case P2SH:
            // https://groups.google.com/d/msg/bitcoinj/pHv4XyMbhZo/RLAkreuBAwAJ
            Script script = ScriptBuilder.createP2WPKHOutputScript(key.getPubKeyHash());
            return LegacyAddress.fromScriptHash(params, Utils.sha256hash160(script.getProgram()));
        case BECH32:
            return SegwitAddress.fromHash(params, key.getPubKeyHash());
        default:
            throw new IllegalArgumentException();
        }
    }
}
