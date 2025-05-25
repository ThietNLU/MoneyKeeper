package ood.application.moneykeeper.controller.factory;

import ood.application.moneykeeper.controller.IStatisticsController;
import ood.application.moneykeeper.controller.StatisticsController;
import ood.application.moneykeeper.model.WalletManager;

public class StatisticsControllerFactory {
    public static IStatisticsController create(WalletManager walletManager) {
        return new StatisticsController(walletManager);
    }
}
