package io.github.cloudiator.persistance;

public interface DomainRepository<D> {

  D save(D d);

  void delete(D d);

  D findById();
}
