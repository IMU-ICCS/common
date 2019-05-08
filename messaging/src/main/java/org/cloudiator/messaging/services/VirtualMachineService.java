package org.cloudiator.messaging.services;

import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messages.Vm.DeleteVirtualMachineRequestMessage;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messages.Vm.VirtualMachineDeletedResponse;
import org.cloudiator.messages.Vm.VirtualMachineEvent;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.ResponseException;

/**
 * Created by daniel on 27.06.17.
 */
public interface VirtualMachineService {

  VirtualMachineCreatedResponse createVirtualMachine(
      CreateVirtualMachineRequestMessage virtualMachineRequestRequest)
      throws ResponseException;

  void createVirtualMachineAsync(CreateVirtualMachineRequestMessage virtualMachineRequestMessage,
      ResponseCallback<VirtualMachineCreatedResponse> callback);

  void deleteVirtualMachineAsync(DeleteVirtualMachineRequestMessage virtualMachineRequestMessage,
      ResponseCallback<VirtualMachineDeletedResponse> callback);

  void announceEvent(VirtualMachineEvent virtualMachineEvent);
}
