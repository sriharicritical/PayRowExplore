package com.payment.payrowapp.sunmipay;


import java.io.Serializable;


public class PayDetail implements Serializable {


    private static final long serialVersionUID = -4488181921964166169L;


    public Long PID;

    public byte[] originalSendBag;  // 未加密的发送包
    public byte[] sendBag;          // 发送包
    public byte[] receiverBag;      // 接收包

    public String TC;
    public String AID;
    public String ATC;
    public String TSI;
    public String TVR;
    public String CID;
    public String ARqC;
    public String ARpC;
    public String appName;     // 应用名称
    public String appLabel;    // 应用标签

    public long tips;
    public long amount;                         // 交易金额 以分为单位
    public long additionalAmount;               // 附加金额 以分为单位

    public int cardType;                        // 卡类型
    public String cardNo = "";                  // 卡号
    public String expDate = "";                 // 卡有效期
    public String cardSerialNo = "";            // 主账号序号
    public String cardHolderName = "";          // 持卡人姓名
    public String track1 = "";
    public String track2 = "";
    public String track3 = "";

    public String currency = "";                // 交易货币
    public String currencyCode = "";            // 货币代码

    public String pinCipher;                    // PIN加密密文
    public long terminalDate;                   // 本地交易时间戳
    public String operatorNo = "";              // 操作员代码
    public String processCode = "";             // 交易处理码
    public String signHexData = "";             // 电子签名十六进制数据
    public String accountType = "";
    public String netManageCode = "";           // 网络管理信息码

    public String ic55Str;
    public String resIc55Str;

    public String terminalNo = "";          // 终端号
    public String merchantNo = "";          // 商户编号
    public String merchantName = "";        // 商户名称

    // 0800.签到
    public String request8583MessageType = "";

    // 22.消费 23.消费撤销 25.退货
    // 10.预授权 11.预授权撤销 20.预授权完成 21.预授权完成撤销
    // 41.小费调整
    // 51.签到 52.签退 53.结算
    public String request8583TransactionType = "";

    // 1.消费 2.消费撤销 3.退货 4.预授权 5.预授权撤销 6.预授权完成 7.预授权完成撤销
    // 31.余额查询
    // 41.小费调整
    // 51.签到 52.签退 53.结算 54.批上送 55. 批上送完成
    public int transactionType;

    // 支持的交易平台: 0：银行卡 1：支付宝 2:微信 3:银联钱包 5:GrabPay
    public int transactionPlatform;

    public String scanCodeValue;                // 被扫时回填二维码数据
    public int scanCodeScanModel;               // 1.主扫 2.被扫
    public int scanCodeTransactionState;        // 扫码交易状态 1：成功；-1：失败 2：支付中

    public String authNo = "";                  // 授权号
    public String referNo = "";                 // 交易参考号
    public String batchNo = "";                 // 交易批次号
    public String voucherNo = "";               // 凭证号(终端/POS流水号)
    public String thirdTradeNo = "";            // 第三方平台交易号
    public String customOrderNo = "";           // 自定义订单号

    public String remark = "";                  // 备注
    public String tradeDate = "";               // 交易日期
    public String tradeTime = "";               // 交易时间
    public String settleDate = "";              // 清算日期

    public long originalAmount;                 // 原交易金额
    public String originalAuthNo = "";          // 原授权号
    public String originalReferNo = "";         // 原交易参考号
    public String originalBatchNo = "";         // 原交易批次号
    public String originalVoucherNo = "";       // 原交易流水号
    public String originalTradeDate = "";       // 原交易日期
    public String originalTradeTime = "";       // 原交易时间
    public String originalThirdTradeNO = "";    // 原交易订单号

    public String tempKSN = "";                 // 当前交易的KSN
    public String tempTerminalNo = "";          // 当前交易独有的终端号
    public String tempMerchantNo = "";          // 当前交易独有的商户号
    public String tempMerchantName = "";        // 当前交易独有的商户名

    public String issuingBankCode = "";         // 发卡机构代码
    public String acquiringBankCode = "";       // 收单机构代码
    public String issuingBankAppData = "";      // 发卡行应用数据
    public String acquiringBankSpotCode = "";   // 收单机构标识码

    public String tradeResultDes = "";          // 交易结果描述
    public String tradeAnswerCode = "";         // 交易应答码

    public String correctReason = "";          // 冲正原因
    public String correctAuthCode = "";        // 冲正授权码
    public String correctAnswerCode = "";      // 冲正应答码

    public String scriptResult = "";            // 脚本处理结果
    public String scriptContent1 = "";          // 脚本1内容(用于二磁授权处理)71
    public String scriptContent2 = "";          // 脚本2内容(用于二磁授权处理)72
    public int scriptUploadStatus = 0;          // 脚本上送状态 0无需脚本状态上送 1需要脚本状态上送 2需要脚本状态上送且上送成功 3需要脚本上送且上送失败
    public String scriptConfirmBag = "";        // 二次授权处理确认包
    public String scriptCorrectBag = "";        // 二次授权处理冲正包
    public String scriptIssuerVerifyData = "";  // 发卡行认证数据

    public boolean isNeedReversal;              // 是否需要冲正
    public boolean isNeedSignature;             // 是否需要签名

    public boolean isAdjust;                    // 是否已调整
    public boolean isOffLine;                   // 是否离线交易
    public boolean isFreePWD;                   // 是否免密
    public boolean isFreeSign;                  // 是否免签
    public boolean isForeignCard;               // 是否是外卡

    public boolean isEasyProcess;
    public boolean isMagneticMode;              // 磁条卡模式
    public boolean isFallbackMode;              // Fallback模式

    public boolean isPrinted;                   // 是否已打印
    public boolean isCanceled;                  // 是否已撤销
    public boolean isReversal;                  // 是否已冲正
    public boolean isReturnGood;                // 是否已退货

    public boolean isTCUploaded;                // 是否已上送TC
    public boolean isSignUpload;                // 电子签名是否上送


    public PayDetail(Long PID, byte[] originalSendBag, byte[] sendBag, byte[] receiverBag, String TC, String AID,
                     String ATC, String TSI, String TVR, String CID, String ARqC, String ARpC, String appName, String appLabel,
                     long tips, long amount, long additionalAmount, int cardType, String cardNo, String expDate, String cardSerialNo,
                     String cardHolderName, String track1, String track2, String track3, String currency, String currencyCode,
                     String pinCipher, long terminalDate, String operatorNo, String processCode, String signHexData,
                     String accountType, String netManageCode, String ic55Str, String resIc55Str, String terminalNo,
                     String merchantNo, String merchantName, String request8583MessageType, String request8583TransactionType,
                     int transactionType, int transactionPlatform, String scanCodeValue, int scanCodeScanModel,
                     int scanCodeTransactionState, String authNo, String referNo, String batchNo, String voucherNo,
                     String thirdTradeNo, String customOrderNo, String remark, String tradeDate, String tradeTime, String settleDate,
                     long originalAmount, String originalAuthNo, String originalReferNo, String originalBatchNo,
                     String originalVoucherNo, String originalTradeDate, String originalTradeTime, String originalThirdTradeNO,
                     String tempKSN, String tempTerminalNo, String tempMerchantNo, String tempMerchantName, String issuingBankCode,
                     String acquiringBankCode, String issuingBankAppData, String acquiringBankSpotCode, String tradeResultDes,
                     String tradeAnswerCode, String correctReason, String correctAuthCode, String correctAnswerCode,
                     String scriptResult, String scriptContent1, String scriptContent2, int scriptUploadStatus,
                     String scriptConfirmBag, String scriptCorrectBag, String scriptIssuerVerifyData, boolean isNeedReversal,
                     boolean isNeedSignature, boolean isAdjust, boolean isOffLine, boolean isFreePWD, boolean isFreeSign,
                     boolean isForeignCard, boolean isEasyProcess, boolean isMagneticMode, boolean isFallbackMode, boolean isPrinted,
                     boolean isCanceled, boolean isReversal, boolean isReturnGood, boolean isTCUploaded, boolean isSignUpload) {
        this.PID = PID;
        this.originalSendBag = originalSendBag;
        this.sendBag = sendBag;
        this.receiverBag = receiverBag;
        this.TC = TC;
        this.AID = AID;
        this.ATC = ATC;
        this.TSI = TSI;
        this.TVR = TVR;
        this.CID = CID;
        this.ARqC = ARqC;
        this.ARpC = ARpC;
        this.appName = appName;
        this.appLabel = appLabel;
        this.tips = tips;
        this.amount = amount;
        this.additionalAmount = additionalAmount;
        this.cardType = cardType;
        this.cardNo = cardNo;
        this.expDate = expDate;
        this.cardSerialNo = cardSerialNo;
        this.cardHolderName = cardHolderName;
        this.track1 = track1;
        this.track2 = track2;
        this.track3 = track3;
        this.currency = currency;
        this.currencyCode = currencyCode;
        this.pinCipher = pinCipher;
        this.terminalDate = terminalDate;
        this.operatorNo = operatorNo;
        this.processCode = processCode;
        this.signHexData = signHexData;
        this.accountType = accountType;
        this.netManageCode = netManageCode;
        this.ic55Str = ic55Str;
        this.resIc55Str = resIc55Str;
        this.terminalNo = terminalNo;
        this.merchantNo = merchantNo;
        this.merchantName = merchantName;
        this.request8583MessageType = request8583MessageType;
        this.request8583TransactionType = request8583TransactionType;
        this.transactionType = transactionType;
        this.transactionPlatform = transactionPlatform;
        this.scanCodeValue = scanCodeValue;
        this.scanCodeScanModel = scanCodeScanModel;
        this.scanCodeTransactionState = scanCodeTransactionState;
        this.authNo = authNo;
        this.referNo = referNo;
        this.batchNo = batchNo;
        this.voucherNo = voucherNo;
        this.thirdTradeNo = thirdTradeNo;
        this.customOrderNo = customOrderNo;
        this.remark = remark;
        this.tradeDate = tradeDate;
        this.tradeTime = tradeTime;
        this.settleDate = settleDate;
        this.originalAmount = originalAmount;
        this.originalAuthNo = originalAuthNo;
        this.originalReferNo = originalReferNo;
        this.originalBatchNo = originalBatchNo;
        this.originalVoucherNo = originalVoucherNo;
        this.originalTradeDate = originalTradeDate;
        this.originalTradeTime = originalTradeTime;
        this.originalThirdTradeNO = originalThirdTradeNO;
        this.tempKSN = tempKSN;
        this.tempTerminalNo = tempTerminalNo;
        this.tempMerchantNo = tempMerchantNo;
        this.tempMerchantName = tempMerchantName;
        this.issuingBankCode = issuingBankCode;
        this.acquiringBankCode = acquiringBankCode;
        this.issuingBankAppData = issuingBankAppData;
        this.acquiringBankSpotCode = acquiringBankSpotCode;
        this.tradeResultDes = tradeResultDes;
        this.tradeAnswerCode = tradeAnswerCode;
        this.correctReason = correctReason;
        this.correctAuthCode = correctAuthCode;
        this.correctAnswerCode = correctAnswerCode;
        this.scriptResult = scriptResult;
        this.scriptContent1 = scriptContent1;
        this.scriptContent2 = scriptContent2;
        this.scriptUploadStatus = scriptUploadStatus;
        this.scriptConfirmBag = scriptConfirmBag;
        this.scriptCorrectBag = scriptCorrectBag;
        this.scriptIssuerVerifyData = scriptIssuerVerifyData;
        this.isNeedReversal = isNeedReversal;
        this.isNeedSignature = isNeedSignature;
        this.isAdjust = isAdjust;
        this.isOffLine = isOffLine;
        this.isFreePWD = isFreePWD;
        this.isFreeSign = isFreeSign;
        this.isForeignCard = isForeignCard;
        this.isEasyProcess = isEasyProcess;
        this.isMagneticMode = isMagneticMode;
        this.isFallbackMode = isFallbackMode;
        this.isPrinted = isPrinted;
        this.isCanceled = isCanceled;
        this.isReversal = isReversal;
        this.isReturnGood = isReturnGood;
        this.isTCUploaded = isTCUploaded;
        this.isSignUpload = isSignUpload;
    }

    public PayDetail() {

    }

    public Long getPID() {
        return this.PID;
    }

    public void setPID(Long PID) {
        this.PID = PID;
    }

    public byte[] getSendBag() {
        return this.sendBag;
    }

    public void setSendBag(byte[] sendBag) {
        this.sendBag = sendBag;
    }

    public byte[] getReceiverBag() {
        return this.receiverBag;
    }

    public void setReceiverBag(byte[] receiverBag) {
        this.receiverBag = receiverBag;
    }

    public String getTC() {
        return this.TC;
    }

    public void setTC(String TC) {
        this.TC = TC;
    }

    public String getAID() {
        return this.AID;
    }

    public void setAID(String AID) {
        this.AID = AID;
    }

    public String getATC() {
        return this.ATC;
    }

    public void setATC(String ATC) {
        this.ATC = ATC;
    }

    public String getTSI() {
        return this.TSI;
    }

    public void setTSI(String TSI) {
        this.TSI = TSI;
    }

    public String getTVR() {
        return this.TVR;
    }

    public void setTVR(String TVR) {
        this.TVR = TVR;
    }

    public String getCID() {
        return this.CID;
    }

    public void setCID(String CID) {
        this.CID = CID;
    }

    public String getARqC() {
        return this.ARqC;
    }

    public void setARqC(String ARqC) {
        this.ARqC = ARqC;
    }

    public String getARpC() {
        return this.ARpC;
    }

    public void setARpC(String ARpC) {
        this.ARpC = ARpC;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppLabel() {
        return this.appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public long getTips() {
        return this.tips;
    }

    public void setTips(long tips) {
        this.tips = tips;
    }

    public long getAmount() {
        return this.amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getAdditionalAmount() {
        return this.additionalAmount;
    }

    public void setAdditionalAmount(long additionalAmount) {
        this.additionalAmount = additionalAmount;
    }

    public int getCardType() {
        return this.cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getExpDate() {
        return this.expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCardSerialNo() {
        return this.cardSerialNo;
    }

    public void setCardSerialNo(String cardSerialNo) {
        this.cardSerialNo = cardSerialNo;
    }

    public String getCardHolderName() {
        return this.cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getTrack1() {
        return this.track1;
    }

    public void setTrack1(String track1) {
        this.track1 = track1;
    }

    public String getTrack2() {
        return this.track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getTrack3() {
        return this.track3;
    }

    public void setTrack3(String track3) {
        this.track3 = track3;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getPinCipher() {
        return this.pinCipher;
    }

    public void setPinCipher(String pinCipher) {
        this.pinCipher = pinCipher;
    }

    public long getTerminalDate() {
        return this.terminalDate;
    }

    public void setTerminalDate(long terminalDate) {
        this.terminalDate = terminalDate;
    }

    public String getOperatorNo() {
        return this.operatorNo;
    }

    public void setOperatorNo(String operatorNo) {
        this.operatorNo = operatorNo;
    }

    public String getProcessCode() {
        return this.processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getSignHexData() {
        return this.signHexData;
    }

    public void setSignHexData(String signHexData) {
        this.signHexData = signHexData;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getNetManageCode() {
        return this.netManageCode;
    }

    public void setNetManageCode(String netManageCode) {
        this.netManageCode = netManageCode;
    }

    public String getIc55Str() {
        return this.ic55Str;
    }

    public void setIc55Str(String ic55Str) {
        this.ic55Str = ic55Str;
    }

    public String getResIc55Str() {
        return this.resIc55Str;
    }

    public void setResIc55Str(String resIc55Str) {
        this.resIc55Str = resIc55Str;
    }

    public String getTerminalNo() {
        return this.terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getMerchantNo() {
        return this.merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getRequest8583MessageType() {
        return this.request8583MessageType;
    }

    public void setRequest8583MessageType(String request8583MessageType) {
        this.request8583MessageType = request8583MessageType;
    }

    public String getRequest8583TransactionType() {
        return this.request8583TransactionType;
    }

    public void setRequest8583TransactionType(String request8583TransactionType) {
        this.request8583TransactionType = request8583TransactionType;
    }

    public int getTransactionType() {
        return this.transactionType;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public int getTransactionPlatform() {
        return this.transactionPlatform;
    }

    public void setTransactionPlatform(int transactionPlatform) {
        this.transactionPlatform = transactionPlatform;
    }

    public String getScanCodeValue() {
        return this.scanCodeValue;
    }

    public void setScanCodeValue(String scanCodeValue) {
        this.scanCodeValue = scanCodeValue;
    }

    public int getScanCodeScanModel() {
        return this.scanCodeScanModel;
    }

    public void setScanCodeScanModel(int scanCodeScanModel) {
        this.scanCodeScanModel = scanCodeScanModel;
    }

    public int getScanCodeTransactionState() {
        return this.scanCodeTransactionState;
    }

    public void setScanCodeTransactionState(int scanCodeTransactionState) {
        this.scanCodeTransactionState = scanCodeTransactionState;
    }

    public String getAuthNo() {
        return this.authNo;
    }

    public void setAuthNo(String authNo) {
        this.authNo = authNo;
    }

    public String getReferNo() {
        return this.referNo;
    }

    public void setReferNo(String referNo) {
        this.referNo = referNo;
    }

    public String getBatchNo() {
        return this.batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getVoucherNo() {
        return this.voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public String getThirdTradeNo() {
        return this.thirdTradeNo;
    }

    public void setThirdTradeNo(String thirdTradeNo) {
        this.thirdTradeNo = thirdTradeNo;
    }

    public String getCustomOrderNo() {
        return this.customOrderNo;
    }

    public void setCustomOrderNo(String customOrderNo) {
        this.customOrderNo = customOrderNo;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTradeDate() {
        return this.tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getTradeTime() {
        return this.tradeTime;
    }

    public void setTradeTime(String tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getSettleDate() {
        return this.settleDate;
    }

    public void setSettleDate(String settleDate) {
        this.settleDate = settleDate;
    }

    public long getOriginalAmount() {
        return this.originalAmount;
    }

    public void setOriginalAmount(long originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getOriginalAuthNo() {
        return this.originalAuthNo;
    }

    public void setOriginalAuthNo(String originalAuthNo) {
        this.originalAuthNo = originalAuthNo;
    }

    public String getOriginalReferNo() {
        return this.originalReferNo;
    }

    public void setOriginalReferNo(String originalReferNo) {
        this.originalReferNo = originalReferNo;
    }

    public String getOriginalBatchNo() {
        return this.originalBatchNo;
    }

    public void setOriginalBatchNo(String originalBatchNo) {
        this.originalBatchNo = originalBatchNo;
    }

    public String getOriginalVoucherNo() {
        return this.originalVoucherNo;
    }

    public void setOriginalVoucherNo(String originalVoucherNo) {
        this.originalVoucherNo = originalVoucherNo;
    }

    public String getOriginalTradeDate() {
        return this.originalTradeDate;
    }

    public void setOriginalTradeDate(String originalTradeDate) {
        this.originalTradeDate = originalTradeDate;
    }

    public String getOriginalTradeTime() {
        return this.originalTradeTime;
    }

    public void setOriginalTradeTime(String originalTradeTime) {
        this.originalTradeTime = originalTradeTime;
    }

    public String getOriginalThirdTradeNO() {
        return this.originalThirdTradeNO;
    }

    public void setOriginalThirdTradeNO(String originalThirdTradeNO) {
        this.originalThirdTradeNO = originalThirdTradeNO;
    }

    public String getTempKSN() {
        return this.tempKSN;
    }

    public void setTempKSN(String tempKSN) {
        this.tempKSN = tempKSN;
    }

    public String getTempTerminalNo() {
        return this.tempTerminalNo;
    }

    public void setTempTerminalNo(String tempTerminalNo) {
        this.tempTerminalNo = tempTerminalNo;
    }

    public String getTempMerchantNo() {
        return this.tempMerchantNo;
    }

    public void setTempMerchantNo(String tempMerchantNo) {
        this.tempMerchantNo = tempMerchantNo;
    }

    public String getTempMerchantName() {
        return this.tempMerchantName;
    }

    public void setTempMerchantName(String tempMerchantName) {
        this.tempMerchantName = tempMerchantName;
    }

    public String getIssuingBankCode() {
        return this.issuingBankCode;
    }

    public void setIssuingBankCode(String issuingBankCode) {
        this.issuingBankCode = issuingBankCode;
    }

    public String getAcquiringBankCode() {
        return this.acquiringBankCode;
    }

    public void setAcquiringBankCode(String acquiringBankCode) {
        this.acquiringBankCode = acquiringBankCode;
    }

    public String getIssuingBankAppData() {
        return this.issuingBankAppData;
    }

    public void setIssuingBankAppData(String issuingBankAppData) {
        this.issuingBankAppData = issuingBankAppData;
    }

    public String getAcquiringBankSpotCode() {
        return this.acquiringBankSpotCode;
    }

    public void setAcquiringBankSpotCode(String acquiringBankSpotCode) {
        this.acquiringBankSpotCode = acquiringBankSpotCode;
    }

    public String getTradeResultDes() {
        return this.tradeResultDes;
    }

    public void setTradeResultDes(String tradeResultDes) {
        this.tradeResultDes = tradeResultDes;
    }

    public String getTradeAnswerCode() {
        return this.tradeAnswerCode;
    }

    public void setTradeAnswerCode(String tradeAnswerCode) {
        this.tradeAnswerCode = tradeAnswerCode;
    }

    public String getCorrectReason() {
        return this.correctReason;
    }

    public void setCorrectReason(String correctReason) {
        this.correctReason = correctReason;
    }

    public String getCorrectAuthCode() {
        return this.correctAuthCode;
    }

    public void setCorrectAuthCode(String correctAuthCode) {
        this.correctAuthCode = correctAuthCode;
    }

    public String getCorrectAnswerCode() {
        return this.correctAnswerCode;
    }

    public void setCorrectAnswerCode(String correctAnswerCode) {
        this.correctAnswerCode = correctAnswerCode;
    }

    public String getScriptResult() {
        return this.scriptResult;
    }

    public void setScriptResult(String scriptResult) {
        this.scriptResult = scriptResult;
    }

    public String getScriptContent1() {
        return this.scriptContent1;
    }

    public void setScriptContent1(String scriptContent1) {
        this.scriptContent1 = scriptContent1;
    }

    public String getScriptContent2() {
        return this.scriptContent2;
    }

    public void setScriptContent2(String scriptContent2) {
        this.scriptContent2 = scriptContent2;
    }

    public int getScriptUploadStatus() {
        return this.scriptUploadStatus;
    }

    public void setScriptUploadStatus(int scriptUploadStatus) {
        this.scriptUploadStatus = scriptUploadStatus;
    }

    public String getScriptConfirmBag() {
        return this.scriptConfirmBag;
    }

    public void setScriptConfirmBag(String scriptConfirmBag) {
        this.scriptConfirmBag = scriptConfirmBag;
    }

    public String getScriptCorrectBag() {
        return this.scriptCorrectBag;
    }

    public void setScriptCorrectBag(String scriptCorrectBag) {
        this.scriptCorrectBag = scriptCorrectBag;
    }

    public String getScriptIssuerVerifyData() {
        return this.scriptIssuerVerifyData;
    }

    public void setScriptIssuerVerifyData(String scriptIssuerVerifyData) {
        this.scriptIssuerVerifyData = scriptIssuerVerifyData;
    }

    public boolean getIsNeedReversal() {
        return this.isNeedReversal;
    }

    public void setIsNeedReversal(boolean isNeedReversal) {
        this.isNeedReversal = isNeedReversal;
    }

    public boolean getIsNeedSignature() {
        return this.isNeedSignature;
    }

    public void setIsNeedSignature(boolean isNeedSignature) {
        this.isNeedSignature = isNeedSignature;
    }

    public boolean getIsAdjust() {
        return this.isAdjust;
    }

    public void setIsAdjust(boolean isAdjust) {
        this.isAdjust = isAdjust;
    }

    public boolean getIsOffLine() {
        return this.isOffLine;
    }

    public void setIsOffLine(boolean isOffLine) {
        this.isOffLine = isOffLine;
    }

    public boolean getIsFreePWD() {
        return this.isFreePWD;
    }

    public void setIsFreePWD(boolean isFreePWD) {
        this.isFreePWD = isFreePWD;
    }

    public boolean getIsFreeSign() {
        return this.isFreeSign;
    }

    public void setIsFreeSign(boolean isFreeSign) {
        this.isFreeSign = isFreeSign;
    }

    public boolean getIsForeignCard() {
        return this.isForeignCard;
    }

    public void setIsForeignCard(boolean isForeignCard) {
        this.isForeignCard = isForeignCard;
    }

    public boolean getIsEasyProcess() {
        return this.isEasyProcess;
    }

    public void setIsEasyProcess(boolean isEasyProcess) {
        this.isEasyProcess = isEasyProcess;
    }

    public boolean getIsMagneticMode() {
        return this.isMagneticMode;
    }

    public void setIsMagneticMode(boolean isMagneticMode) {
        this.isMagneticMode = isMagneticMode;
    }

    public boolean getIsFallbackMode() {
        return this.isFallbackMode;
    }

    public void setIsFallbackMode(boolean isFallbackMode) {
        this.isFallbackMode = isFallbackMode;
    }

    public boolean getIsPrinted() {
        return this.isPrinted;
    }

    public void setIsPrinted(boolean isPrinted) {
        this.isPrinted = isPrinted;
    }

    public boolean getIsCanceled() {
        return this.isCanceled;
    }

    public void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public boolean getIsReversal() {
        return this.isReversal;
    }

    public void setIsReversal(boolean isReversal) {
        this.isReversal = isReversal;
    }

    public boolean getIsReturnGood() {
        return this.isReturnGood;
    }

    public void setIsReturnGood(boolean isReturnGood) {
        this.isReturnGood = isReturnGood;
    }

    public boolean getIsTCUploaded() {
        return this.isTCUploaded;
    }

    public void setIsTCUploaded(boolean isTCUploaded) {
        this.isTCUploaded = isTCUploaded;
    }

    public boolean getIsSignUpload() {
        return this.isSignUpload;
    }

    public void setIsSignUpload(boolean isSignUpload) {
        this.isSignUpload = isSignUpload;
    }

    public byte[] getOriginalSendBag() {
        return this.originalSendBag;
    }

    public void setOriginalSendBag(byte[] originalSendBag) {
        this.originalSendBag = originalSendBag;
    }


}
