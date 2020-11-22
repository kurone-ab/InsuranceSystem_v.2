package system.insurance.backend.resource.service;

import system.insurance.backend.accident.AccidentType;
import system.insurance.backend.exception.NoClientException;
import system.insurance.backend.resource.dto.ClientDTO;
import system.insurance.backend.resource.dto.ContractDTO;

import java.util.Date;
import java.util.List;

public interface AccidentService {
    List<ContractDTO> checkRegisteredClient(String name, String rrn) throws NoClientException;

    boolean addAccident(int contractId, String accidentArea, AccidentType accidentType, Date date);
}