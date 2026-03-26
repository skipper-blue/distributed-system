package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteService extends Remote {
    String placeOrder(Order order) throws RemoteException;
    List<String> getCustomers() throws RemoteException;
    List<String> getBranchReport() throws RemoteException;
    List<String> getRevenuePerBranch() throws RemoteException;
    double getTotalRevenue() throws RemoteException;
    List<String> getLowStockAlerts() throws RemoteException;
    List<Drink> getAvailableDrinks() throws RemoteException;
}
