package payroll;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Web {@link RestController} used to generate a REST API.
 *
 */
@RestController
class EmployeeController {

	private final EmployeeRepository repository;

	private final EmployeeModelAssembler assembler;

	EmployeeController(EmployeeRepository repository, EmployeeModelAssembler assembler) {
	  this.repository = repository;
	  this.assembler = assembler;
	}
	  
	/**
	 * Look up all employees, and transform them into a REST collection resource. Then return them through Spring Web's
	 * {@link ResponseEntity} fluent API.
	 */
//	@GetMapping("/employees")
//	ResponseEntity<CollectionModel<EntityModel<Employee>>> findAll() {
//
//		List<EntityModel<Employee>> employees = StreamSupport.stream(repository.findAll().spliterator(), false)
//				.map(employee -> EntityModel.of(employee, //
//						linkTo(methodOn(EmployeeController.class).findOne(employee.getId())).withSelfRel(), //
//						linkTo(methodOn(EmployeeController.class).findAll()).withRel("employees"))) //
//				.collect(Collectors.toList());
//
//		return ResponseEntity.ok( //
//				CollectionModel.of(employees, //
//						linkTo(methodOn(EmployeeController.class).findAll()).withSelfRel()));
//	}
//	@GetMapping("/employees")
//	CollectionModel<EntityModel<Employee>> findAll() {
//
//	  List<EntityModel<Employee>> employees = repository.findAll().stream()
//	      .map(employee -> EntityModel.of(employee,
//	          linkTo(methodOn(EmployeeController.class).findOne(employee.getId())).withSelfRel(),
//	          linkTo(methodOn(EmployeeController.class).findAll()).withRel("employees")))
//	      .collect(Collectors.toList());
//
//	  return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).findAll()).withSelfRel());
//	}

	@GetMapping("/employees")
	CollectionModel<EntityModel<Employee>> findAll() {

	  List<EntityModel<Employee>> employees = repository.findAll().stream() //
	      .map(assembler::toModel) //
	      .collect(Collectors.toList());

	  return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).findAll()).withSelfRel());
	}
	
	@PostMapping("/employees")
	ResponseEntity<?> newEmployee(@RequestBody Employee employee) {

		try {
			Employee savedEmployee = repository.save(employee);

			EntityModel<Employee> employeeResource = EntityModel.of(savedEmployee, //
					linkTo(methodOn(EmployeeController.class).findOne(savedEmployee.getId())).withSelfRel());

			return ResponseEntity //
					.created(new URI(employeeResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
					.body(employeeResource);
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to create " + employee);
		}
	}

	/**
	 * Look up a single {@link Employee} and transform it into a REST resource. Then return it through Spring Web's
	 * {@link ResponseEntity} fluent API.
	 *
	 * @param id
	 */
//	@GetMapping("/employees/{id}")
//	ResponseEntity<EntityModel<Employee>> findOne(@PathVariable long id) {
//
//		return repository.findById(id) //
//				.map(employee -> EntityModel.of(employee, //
//						linkTo(methodOn(EmployeeController.class).findOne(employee.getId())).withSelfRel(), //
//						linkTo(methodOn(EmployeeController.class).findAll()).withRel("employees"))) //
//				.map(ResponseEntity::ok) //
//				.orElse(ResponseEntity.notFound().build());
//	}
//	
	@GetMapping("/employees/{id}")
	EntityModel<Employee> findOne(@PathVariable Long id) {

	  Employee employee = repository.findById(id) //
	      .orElseThrow(() -> new EmployeeNotFoundException(id));

	  return assembler.toModel(employee);
	}
	
	
	
//	@GetMapping("/employees/{id}")
//	EntityModel<Employee> one(@PathVariable Long id) {
//
//	  Employee employee = repository.findById(id) //
//	      .orElseThrow(() -> new EmployeeNotFoundException(id));
//
//	  return EntityModel.of(employee, //
//	      linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(),
//	      linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
//	}

	/**
	 * Update existing employee then return a Location header.
	 * 
	 * @param employee
	 * @param id
	 * @return
	 */
	@PutMapping("/employees/{id}")
	ResponseEntity<?> updateEmployee(@RequestBody Employee employee, @PathVariable long id) {

		Employee employeeToUpdate = employee;
		employeeToUpdate.setId(id);
		repository.save(employeeToUpdate);

		Link newlyCreatedLink = linkTo(methodOn(EmployeeController.class).findOne(id)).withSelfRel();

		try {
			return ResponseEntity.noContent().location(new URI(newlyCreatedLink.getHref())).build();
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to update " + employeeToUpdate);
		}
	}

}