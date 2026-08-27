import { useState } from 'react'
import type { MeusDadosPayload, Pessoa } from '../domain/pessoa.types'
import { useAtualizarMeusDados } from './useMeusDados'

const TELEFONE_VALIDO = /^\(?\d{2}\)?\s?9?\d{4}-?\d{4}$/

type Campos = Omit<MeusDadosPayload, 'telefone'> & { telefone: string }

function camposDe(pessoa: Pessoa | undefined): Campos {
  return {
    telefone: pessoa?.telefone ?? '',
    alergias: pessoa?.alergias ?? '',
    restricaoAlimentar: pessoa?.restricaoAlimentar ?? '',
    medicacaoUsoContinuo: pessoa?.medicacaoUsoContinuo ?? '',
    contatoEmergenciaNome: pessoa?.contatoEmergenciaNome ?? '',
    contatoEmergenciaTelefone: pessoa?.contatoEmergenciaTelefone ?? '',
    contatoEmergenciaParentesco: pessoa?.contatoEmergenciaParentesco ?? '',
  }
}

/**
 * Edição dos próprios dados. O formulário só entra em modo de edição quando a pessoa pede — assim
 * a tela abre como leitura do próprio cadastro, que é o uso mais comum, e não como um formulário.
 */
export function useMeusDadosForm(pessoa: Pessoa | undefined) {
  const atualizar = useAtualizarMeusDados()
  const [editando, setEditando] = useState(false)
  const [campos, setCampos] = useState<Campos>(camposDe(pessoa))
  const [erro, setErro] = useState<string | undefined>(undefined)
  const [salvo, setSalvo] = useState(false)

  function editar() {
    setCampos(camposDe(pessoa))
    setErro(undefined)
    setSalvo(false)
    setEditando(true)
  }

  function cancelar() {
    setEditando(false)
    setErro(undefined)
  }

  function alterar(campo: keyof Campos, valor: string) {
    setCampos((atual) => ({ ...atual, [campo]: valor }))
    setSalvo(false)
  }

  async function salvar() {
    if (!TELEFONE_VALIDO.test(campos.telefone.trim())) {
      setErro('Informe um telefone válido, com DDD.')
      return
    }
    // Campo opcional em branco volta como nulo, não como string vazia: "não informado" e "informado
    // como nada" são coisas diferentes para quem lê a ficha depois.
    const vazioComoNulo = (valor: string | null) => (valor && valor.trim() ? valor.trim() : null)
    try {
      await atualizar.mutateAsync({
        telefone: campos.telefone.trim(),
        alergias: vazioComoNulo(campos.alergias),
        restricaoAlimentar: vazioComoNulo(campos.restricaoAlimentar),
        medicacaoUsoContinuo: vazioComoNulo(campos.medicacaoUsoContinuo),
        contatoEmergenciaNome: vazioComoNulo(campos.contatoEmergenciaNome),
        contatoEmergenciaTelefone: vazioComoNulo(campos.contatoEmergenciaTelefone),
        contatoEmergenciaParentesco: vazioComoNulo(campos.contatoEmergenciaParentesco),
      })
      setEditando(false)
      setSalvo(true)
    } catch {
      setErro('Não foi possível salvar. Tente novamente.')
    }
  }

  return {
    editando,
    campos,
    erro,
    salvo,
    isSalvando: atualizar.isPending,
    editar,
    cancelar,
    alterar,
    salvar,
  }
}
