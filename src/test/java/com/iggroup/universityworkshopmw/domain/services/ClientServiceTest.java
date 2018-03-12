package com.iggroup.universityworkshopmw.domain.services;

import com.iggroup.universityworkshopmw.domain.exceptions.DuplicatedDataException;
import com.iggroup.universityworkshopmw.domain.exceptions.NoAvailableDataException;
import com.iggroup.universityworkshopmw.domain.model.Client;
import org.junit.Before;
import org.junit.Test;

import static com.iggroup.universityworkshopmw.domain.services.ClientService.INITIAL_FUNDS;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientServiceTest {

   private ClientService clientService;

   @Before
   public void setUp() {
      clientService = new ClientService();
   }

   @Test
   public void storeNewClient_assignsUniqueIdPerAddition() throws DuplicatedDataException {
      Client client1 = createClient("userName1");
      Client client2 = createClient("userName2");

      Client returnClient1 = clientService.storeNewClient(client1);
      Client returnClient2 = clientService.storeNewClient(client2);

      assertThat(returnClient1.getId()).isNotNull();
      assertThat(returnClient2.getId()).isNotNull();
      assertThat(returnClient1.getId()).isNotEqualTo(returnClient2.getId());
   }

   @Test
   public void storeNewClient_setsCorrectInitialValues() throws DuplicatedDataException {
      Client client1 = createClient("userName1");

      Client returnClient1 = clientService.storeNewClient(client1);

      assertThat(returnClient1.getAvailableFunds()).isEqualTo(INITIAL_FUNDS);
      assertThat(returnClient1.getRunningProfitAndLoss()).isEqualTo(0);
   }

   @Test
   public void getClientData_getsDataForClientId() throws NoAvailableDataException, DuplicatedDataException {
      final Client expected = createClient("userName1");
      Client returnClient1 = clientService.storeNewClient(expected);
      String clientId = returnClient1.getId();

      Client actual = clientService.getClientData(clientId);

      assertThat(actual).isEqualToIgnoringGivenFields(expected, "id", "availableFunds");
      assertThat(actual.getAvailableFunds()).isEqualTo(INITIAL_FUNDS);
   }

   @Test(expected = NoAvailableDataException.class)
   public void getClientData_handlesMapContainingNoClientDataForClientId() throws NoAvailableDataException {
      String clientId = "randomIdNotInMap";

      clientService.getClientData(clientId);
   }

   @Test
   public void getClientDataByUsername_getsDataForClientUsername() throws NoAvailableDataException, DuplicatedDataException {
      final String userName1 = "userName1";
      final Client expected = createClient(userName1);
      clientService.storeNewClient(expected);

      Client actual = clientService.getClientDataByUsername(userName1);

      assertThat(actual).isEqualToIgnoringGivenFields(expected, "id", "availableFunds");
   }

   @Test(expected = NoAvailableDataException.class)
   public void getClientDataByUsername_handlesMapContainingNoClientDataForClientUsername() throws NoAvailableDataException {
      String username = "usernameNotInMap";

      clientService.getClientDataByUsername(username);
   }

   @Test
   public void updateAvailableFunds_updatesAvailableFunds() throws NoAvailableDataException, DuplicatedDataException {
      Client returnClient1 = clientService.storeNewClient(createClient("userName1"));
      String clientId = returnClient1.getId();
      double availableFunds = returnClient1.getAvailableFunds();
      double fundsUpdate = availableFunds - 200;

      clientService.updateAvailableFunds(clientId, fundsUpdate);

      double returnedFunds = clientService.getClientData(clientId).getAvailableFunds();
      assertThat(fundsUpdate).isEqualTo(returnedFunds);
   }

   @Test(expected = NoAvailableDataException.class)
   public void updateAvailableFunds_handlesMapContainingNoClientDataForClientId() throws NoAvailableDataException {
      String clientId = "randomIdNotInMap";

      clientService.updateAvailableFunds(clientId, 900);
   }

   @Test
   public void updateRunningProfitAndLoss_updatesAvailableFundsAndRunningProfitAndLoss() throws NoAvailableDataException, DuplicatedDataException {
      Client client = clientService.storeNewClient(createClient("userName1"));
      String clientId = client.getId();
      double initialAvailableFunds = client.getAvailableFunds();
      // To account for a previous profit and loss calculation
      clientService.updateRunningProfitAndLoss(clientId, 5);
      double sumOfProfitAndLoss = 500;

      clientService.updateRunningProfitAndLoss(clientId, sumOfProfitAndLoss);

      final Client clientData = clientService.getClientData(clientId);
      assertThat(clientData.getRunningProfitAndLoss()).isEqualTo(sumOfProfitAndLoss);
      assertThat(clientData.getAvailableFunds()).isEqualTo(initialAvailableFunds + sumOfProfitAndLoss);
   }

   @Test(expected = NoAvailableDataException.class)
   public void updateRunningProfitAndLoss_handlesMapContainingNoClientDataForClientId() throws NoAvailableDataException {
      String clientId = "randomIdNotInMap";

      clientService.updateRunningProfitAndLoss(clientId, 800);
   }

   private Client createClient(String userName) {
      return Client.builder()
            .id("clientId")
            .userName(userName)
            .availableFunds(0)
            .runningProfitAndLoss(0)
            .build();
   }
}