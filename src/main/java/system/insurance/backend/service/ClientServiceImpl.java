package system.insurance.backend.service;

import org.springframework.stereotype.Service;
import system.insurance.backend.dbo.client.*;
import system.insurance.backend.dbo.contract.Contract;
import system.insurance.backend.exception.InvalidIdentifierException;
import system.insurance.backend.exception.NoClientException;
import system.insurance.backend.dto.ClientDTO;
import system.insurance.backend.repository.ClientRepository;
import system.insurance.backend.repository.ContractRepository;
import system.insurance.backend.repository.RegisteredClientRepository;

import java.util.*;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final ContractRepository contractRepository;

    public ClientServiceImpl(ClientRepository clientRepository, RegisteredClientRepository registeredClientRepository, ContractRepository contractRepository) {
        this.clientRepository = clientRepository;
        this.registeredClientRepository = registeredClientRepository;
        this.contractRepository = contractRepository;
    }

    @Override
    public Map<Integer, ClientDTO> findAllRegisteringClient() {
        List<Client> clientList = this.clientRepository.findAllByType(ClientType.REGISTERING);
        Map<Integer, ClientDTO> clientDTOList = new HashMap<>();
        clientList.forEach(client -> {
            RegisteringClient client1 = (RegisteringClient) client;
            clientDTOList.put(client1.getId(), ClientDTO.builder().name(client1.getName()).build());
        });
        return clientDTOList;
    }

    @Override
    public List<ClientDTO> findAllUnregisteredClint() {
        List<Client> clientList = this.clientRepository.findAll();
        List<ClientDTO> clientDTOList = new ArrayList<>() ;
        clientList.forEach((client)-> {
            if(client instanceof NotRegisteredClient) {
                System.out.println(client.getName()+" 가망고객님.");
                clientDTOList.add(
                        ClientDTO.builder()
                                .name(client.getName())
                                .id(client.getId())
                                .age(client.getAge())
                                .sex(client.getSex().getDesc())
                                .build());
            }
        });
        return clientDTOList;
    }

    @Override
    public ClientDTO findUnregisteredClientByID(int cid) {
        Optional<Client> client = this.clientRepository.findById(cid);
        if(client.isPresent()) {
         Client client1= client.get();
            return ClientDTO
                    .builder()
                    .contact(client1.getContact())
                    .age(client1.getAge())
                    .sex(client1.getSex().getDesc())
                    .name(client1.getName())
                    .email(client1.getEmail())
                    .build();
        }
        return null;
    }

    @Override
    public ClientDTO getRegisteringClientDetail(int cid) throws NoClientException {
        Optional<Client> client = this.clientRepository.findById(cid);
        RegisteringClient client1 = (RegisteringClient) client.orElseThrow(NoClientException::new);
        Optional<Contract> contract = this.contractRepository.findByClient(client1);
        Contract contract1 = contract.orElseThrow(InvalidIdentifierException::new);
        return ClientDTO.builder()
                .id(client1.getId())
                .age(client1.getAge())
                .contact(client1.getContact())
                .insuranceName(contract1.getInsurance().getName())
                .name(client1.getName())
                .underWritingScore(client1.getUnderWritingScore())
                .build();
    }

    @Override
    public boolean setConformity(int cid, boolean conformity, String reason) {
        Optional<Client> client = this.clientRepository.findById(cid);
        client.ifPresent((client1 -> {
            RegisteringClient client2 = (RegisteringClient) client1;
            client2.setConformity(conformity);
            client2.setReason(reason);
            this.clientRepository.save(client2);
        }));
        return true;
    }

    @Override
    public ClientDTO searchRegisteredByName(String name) {
        Optional<Client> client = this.registeredClientRepository.findByName(name);
        if (client.isPresent()) return getClientDTO(client.get());
        return ClientDTO.builder().build();
    }

    @Override
    public ClientDTO searchRegisteredByContact(String contact) {
        Optional<Client> client = this.registeredClientRepository.findByContact(contact);
        if (client.isPresent()) return getClientDTO(client.get());
        return ClientDTO.builder().build();
    }

    @Override
    public ClientDTO searchRegisteredByRRN(String rrn) {
        Optional<Client> client = this.registeredClientRepository.findByRrn(rrn);
        if (client.isPresent()) return getClientDTO(client.get());
        return ClientDTO.builder().build();
    }



    private ClientDTO getClientDTO(Client client) {
        RegisteredClient registeredClient = (RegisteredClient) client;
        Optional<Contract> contractOptional = this.contractRepository.findByClient(registeredClient);
        Contract contract = contractOptional.orElseThrow(InvalidIdentifierException::new);
        return ClientDTO.builder()
                .id(registeredClient.getId())
                .name(registeredClient.getName())
                .insuranceName(contract.getInsurance().getName())
                .contact(registeredClient.getContact()).build();
    }
}
