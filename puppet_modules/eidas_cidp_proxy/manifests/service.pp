# service.pp
class eidas_cidp_proxy::service inherits eidas_cidp_proxy {

  include platform

  if ($platform::deploy_spring_boot) {
    service { $eidas_cidp_proxy::service_name:
      ensure => running,
      enable => true,
    }
  }
}
