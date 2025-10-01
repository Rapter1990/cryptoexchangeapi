package com.casestudy.cryptoexchangeapi.exchange.model.enums;

public enum EnumCryptoCurrency {

    BTC("Bitcoin"),
    ETH("Ethereum"),
    USDT("Tether"),
    USDC("USD Coin"),
    BNB("BNB"),
    XRP("XRP"),
    SOL("Solana"),
    ADA("Cardano"),
    DOGE("Dogecoin"),
    TRX("TRON"),
    TON("Toncoin"),
    DOT("Polkadot"),
    AVAX("Avalanche"),
    SHIB("Shiba Inu"),
    LINK("Chainlink"),
    MATIC("Polygon"),
    LTC("Litecoin"),
    BCH("Bitcoin Cash"),
    XLM("Stellar"),
    ETC("Ethereum Classic"),
    NEAR("NEAR Protocol"),
    ATOM("Cosmos"),
    XMR("Monero"),
    ICP("Internet Computer"),
    FIL("Filecoin"),
    HBAR("Hedera"),
    APT("Aptos"),
    SUI("Sui"),
    ARB("Arbitrum"),
    OP("Optimism"),
    STX("Stacks"),
    LDO("Lido DAO"),
    INJ("Injective"),
    AAVE("Aave"),
    MKR("Maker"),
    RNDR("Render"),
    IMX("Immutable"),
    SEI("Sei"),
    ALGO("Algorand"),
    SAND("The Sandbox"),
    AXS("Axie Infinity"),
    GRT("The Graph"),
    EGLD("MultiversX"),
    KAS("Kaspa"),
    CRO("Cronos"),
    FTM("Fantom"),
    THETA("Theta Network"),
    VET("VeChain"),
    MNT("Mantle"),
    BTT("BitTorrent"),
    PEPE("Pepe"),
    FLOKI("FLOKI"),
    BONK("Bonk"),
    JUP("Jupiter");

    private final String displayName;

    EnumCryptoCurrency(String displayName) {
        this.displayName = displayName;
    }
}


