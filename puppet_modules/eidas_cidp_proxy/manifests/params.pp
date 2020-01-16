#params.pp
class eidas_cidp_proxy::params {
  $java_home                              = hiera('platform::java_home')
  $server_port                            = 10009
  $eventlog_jms_url                       = hiera('platform::jms_url')
  $eventlog_jms_queuename                 = hiera('idporten_logwriter::jms_queueName')
  $saml_proxy_idp_entity_name             = hiera('idporten_opensso::idp_entity_id')
  $saml_idp_entity_name                   = hiera('idporten_opensso::idp_entity_id')
  $saml_idp_base_url                      = hiera('idporten_opensso::opensso_main_site')
  $mf_gateway_url                         = hiera('mf::mf_gateway_url', 'http://localhost/')
  $mf_gateway_username                    = 'user'
  $mf_gateway_password                    = 'password'
  $saml_proxy_sp_entity_name              = 'eidas-cidp-proxy'
  $sp_url                                 = hiera('eidas_cidp_proxy::sp_url')
  $oidc_enable                            = false
  $oidc_issuer_uri                        = 'https://eid-test-oidc-provider.difi.no/idporten-oidc-provider'
  $oidc_client_id                         = hiera('eidas_cidp_proxy::oidc_client_id')
  $oidc_client_secret                     = hiera('eidas_cidp_proxy::oidc_client_secret')
  $oidc_redirect_uri                      = hiera('eidas_cidp_proxy::oidc_redirect_uri')
  $mf_gateway_timeout                     = 10000
  $mf_gateway_retry_count                 = 2
  $mf_gateway_enabled                     = false
  $log_level                              = 'WARN'
  $auditlog_dir                           = '/var/log/eidas-cidp-proxy/audit/'
  $auditlog_file                          = 'audit.log'
  $eidas_cidp_proxy_dummy_enabled         = false
  $saml_instant_issue_time_to_live        = 300
  $saml_instant_issue_time_skew           = 60
  $eidas_cidp_proxy_timeout               = 10000
  $eidas_cidp_proxy_retry_count           = 2
  $saml_requester                         = 'http://eid.difi.no.cid'
  $saml_responder                         = 'http://eid.difi.no.cid'
  $cidpproxy_keystore_location            = 'cidpproxykeystore.jks'
  $cidpproxy_keystore_password            = 'changeit'
  $cidpproxy_privatekey_password          = 'changeit'
  $cidpproxy_keystore_alias               = 'test'
  $cidpproxy_keystore_type                = 'JKS'
  $cidpproxy_keystore_serial_number       = '478d074b'
  $cidpproxy_keystore_issuer              = 'CN=test, OU=OpenSSO, O=Sun, L=Santa Clara, ST=California, C=US'
  $key_store                              = { storetype => 'JKS', password => 'changeit',name => 'NO_Eidas_Sign_JKS.jks',issuer => ' CN=Norwegian Eidas Test Certificate, OU=SSU, O=Agency for Public Management and eGovernment (Difi), C=NO, EMAILADDRESS=idporten@difi.no',serialNumber =>'55A8DB49',keyPassword => 'changeit', }
  $trust_store                            = { storetype => 'JKS', password => 'changeit',name => 'NO_Eidas_Trust_JKS.jks', }
  $security_providers                     = ''
  $log_root                               = '/var/log/'
  $application                            = 'eidas-cidp-proxy'
  $service_name                           = 'eidas-cidp-proxy'
  $context                                = 'eidas-cidp-proxy'
  $artifact_id                            = 'eidas-cidp-proxy'
  $install_dir                            = '/opt/'
  $config_dir                             = '/etc/opt/'
  $cidpproxy_url                          = hiera('eidas_cidp_proxy::url')
  $health_show_details                    = 'always'
  $tomcat_tmp_dir                         = '/opt/eidas-cidp-proxy/tmp'
  $server_tomcat_max_threads              = 200
  $server_tomcat_min_spare_threads        = 10
}
