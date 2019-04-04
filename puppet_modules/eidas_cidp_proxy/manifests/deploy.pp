#deploy.pp
class eidas_cidp_proxy::deploy inherits eidas_cidp_proxy {

  include 'difilib'

  difilib::spring_boot_deploy { $eidas_cidp_proxy::application:
    package         => 'no.difi.eidas',
    artifact        => $eidas_cidp_proxy::artifact_id,
    service_name    => $eidas_cidp_proxy::service_name,
    install_dir     => "${eidas_cidp_proxy::install_dir}${eidas_cidp_proxy::application}",
    artifact_type   => "war",
  }
}