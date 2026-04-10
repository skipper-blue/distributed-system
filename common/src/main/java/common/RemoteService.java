package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface RemoteService extends Remote {
    String placeOrder(Order order) throws RemoteException;
    List<String> getCustomers() throws RemoteException;
    List<String> getBranchReport() throws RemoteException;
    List<String> getRevenuePerBranch() throws RemoteException;
    double getTotalRevenue() throws RemoteException;
    List<String> getLowStockAlerts() throws RemoteException;
    List<Drink> getAvailableDrinks() throws RemoteException;
    String restockBranch(String branch, String drinkName, int quantity) throws RemoteException;
    boolean restockDrink(String branch, int drinkId, int quantity) throws RemoteException;
    List<String> getRestockHistory() throws RemoteException;
    boolean restockMainBranch(int drinkId, int quantity) throws RemoteException;
    boolean distributeStock(String toBranch, int drinkId, int quantity) throws RemoteException;
    List<String> getDistributionHistory() throws RemoteException;
    List<String> getAllStockLevels() throws RemoteException;
    Map<String, Integer> getStockByBranch(int drinkId) throws RemoteException;
}
